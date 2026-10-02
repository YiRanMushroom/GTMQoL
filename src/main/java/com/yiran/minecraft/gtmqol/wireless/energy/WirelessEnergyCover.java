package com.yiran.minecraft.gtmqol.wireless.energy;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.yiran.minecraft.gtmqol.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.wireless.WirelessBindingTrait;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;

import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.GTValues.V;

/**
 * Moves up to V × A EU per tick between the machine it is on and the bound player's wireless network: input
 * charges the machine, output drains it. Energy is added to / taken from the machine's buffer directly, so
 * the machine's input voltage doesn't matter and nothing explodes; the tier only sets the rate.
 */
public class WirelessEnergyCover extends CoverBehavior implements IMuiCover {

    private final int tier;
    private final IO io;

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    @SaveField
    @SyncToClient
    protected int amperage = WirelessEnergyHatchPartMachine.DEFAULT_AMPERAGE;

    private @Nullable TickableSubscription subscription;

    public WirelessEnergyCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier,
                               IO io) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.io = io;
        this.binding = new WirelessBindingTrait(this, coverHolder::isRemote);
    }

    public int getAmperage() {
        return amperage;
    }

    public void setAmperage(int amperage) {
        this.amperage = WirelessEnergyHatchPartMachine.clampAmperage(amperage);
        syncDataHolder.markClientSyncFieldDirty("amperage");
    }

    private @Nullable IEnergyContainer getEnergyContainer() {
        return GTCapabilityHelper.getEnergyContainer(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide);
    }

    /**
     * Input needs something that takes EU, output something that emits it (generators, battery buffers...).
     */
    @Override
    public boolean canAttach() {
        if (!super.canAttach()) return false;
        IEnergyContainer container = getEnergyContainer();
        if (container == null) return false;
        return io == IO.IN ? container.getInputVoltage() > 0 : container.getOutputVoltage() > 0;
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
        if (player != null && !(player instanceof FakePlayer)) {
            binding.bind(player.getUUID(), false);
            setAmperage(WirelessEnergySavedData.get().getDefaultAmperage(player.getUUID()));
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
        IEnergyContainer container = getEnergyContainer();
        if (container == null) return;
        // At most V[MAX] × MAX_AMPERAGE = 2^31 × 2^24, fits a long.
        long max = V[tier] * amperage;
        var data = WirelessEnergySavedData.get();
        if (io == IO.IN) {
            long paid = data.extract(network, Math.min(max, container.getEnergyCanBeInserted()), 1);
            if (paid <= 0) return;
            long added = container.changeEnergy(paid);
            if (added < paid) data.insert(network, paid - added);
        } else {
            long removed = -container.changeEnergy(-Math.min(max, container.getEnergyStored()));
            data.insert(network, removed);
        }
    }

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        column.child(WirelessEnergyUI.create(syncManager, binding, data.getPlayer())
                .widthRel(1f)
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT + 22)
                .child(WirelessEnergyUI.amperageRow(syncManager, this::getAmperage, this::setAmperage)));
    }
}
