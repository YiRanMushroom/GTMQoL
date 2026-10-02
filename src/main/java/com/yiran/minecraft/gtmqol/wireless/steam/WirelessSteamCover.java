package com.yiran.minecraft.gtmqol.wireless.steam;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.yiran.minecraft.gtmqol.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.wireless.WirelessBindingTrait;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;

/**
 * Moves steam between the machine it is on and the bound player's wireless network, as much as the machine
 * takes or gives each tick: input fills it, output drains it (boilers).
 */
public class WirelessSteamCover extends CoverBehavior implements IMuiCover {

    private final IO io;

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    private @Nullable TickableSubscription subscription;

    public WirelessSteamCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, IO io) {
        super(definition, coverHolder, attachedSide);
        this.io = io;
        this.binding = new WirelessBindingTrait(this, coverHolder::isRemote);
    }

    private @Nullable IFluidHandler getFluidHandler() {
        return FluidUtil.getFluidHandler(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide)
                .orElse(null);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && getFluidHandler() != null;
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
        if (player != null && !(player instanceof FakePlayer)) {
            binding.bind(player.getUUID(), false);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void update() {
        NetworkId network = binding.getNetworkId();
        if (network == null) return;
        IFluidHandler handler = getFluidHandler();
        if (handler == null) return;
        var data = WirelessSteamSavedData.get();
        if (io == IO.IN) {
            int stored = data.getStoredInt(network);
            if (stored <= 0) return;
            int wanted = handler.fill(GTMaterials.Steam.getFluid(stored), FluidAction.SIMULATE);
            int taken = data.extract(network, wanted, false);
            if (taken <= 0) return;
            int filled = handler.fill(GTMaterials.Steam.getFluid(taken), FluidAction.EXECUTE);
            if (filled < taken) data.insert(network, taken - filled);
        } else {
            FluidStack drained = handler.drain(GTMaterials.Steam.getFluid(Integer.MAX_VALUE), FluidAction.EXECUTE);
            if (!drained.isEmpty()) data.insert(network, drained.getAmount());
        }
    }

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        column.child(WirelessSteamUI.create(syncManager, binding, data.getPlayer()).widthRel(1f));
    }
}
