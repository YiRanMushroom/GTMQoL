package com.yiran.minecraft.gtmqol.common.wireless.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessBindingTrait;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ToggleButton;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Standalone block exposing the bound player's wireless steam network as a fluid handler on every side.
 * Pipes can push steam in or pull it out; with auto output on, it also pushes steam out of its front.
 */
public class WirelessSteamAccessorMachine extends MetaMachine implements IMuiMachine, IDataStickInteractable {

    public static final String AUTO_OUTPUT_ON_KEY = "gtmqol.wireless.auto_output.enabled";
    public static final String AUTO_OUTPUT_OFF_KEY = "gtmqol.wireless.auto_output.disabled";

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    private final WirelessSteamTank storage;

    @SaveField
    public final NotifiableFluidTank tank;

    @SaveField
    @SyncToClient
    protected boolean autoOutput;

    private @Nullable TickableSubscription autoOutputSubs;

    public WirelessSteamAccessorMachine(BlockEntityCreationInfo info) {
        super(info);
        this.binding = attachTrait(new WirelessBindingTrait());
        this.storage = new WirelessSteamTank(binding::getNetworkId);
        this.tank = attachTrait(new NotifiableFluidTank(List.of(storage), IO.NONE, IO.BOTH));
        // The front only outputs while auto output is on, so a pipe there can't push the steam straight back.
        tank.setCapabilityValidator(side -> !autoOutput || side != getFrontFacing());
    }

    public boolean isAutoOutput() {
        return autoOutput;
    }

    public void setAutoOutput(boolean autoOutput) {
        this.autoOutput = autoOutput;
        syncDataHolder.markClientSyncFieldDirty("autoOutput");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::autoOutput);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unsubscribe(autoOutputSubs);
        autoOutputSubs = null;
    }

    private void autoOutput() {
        if (!autoOutput || getOffsetTimer() % 5 != 0) return;
        // Straight from the storage: NotifiableFluidTank caches isEmpty, which other machines on the network
        // don't invalidate.
        GTTransferUtils.getAdjacentFluidHandler(getLevel(), getBlockPos(), getFrontFacing())
                .ifPresent(target -> GTTransferUtils.transferFluidsFiltered(storage, target, storage::isFluidValid));
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        return binding.onDataStickUse(player, dataStick);
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        return binding.onDataStickShiftUse(player, dataStick);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var autoOutputValue = new BooleanSyncValue(this::isAutoOutput, this::setAutoOutput).allowC2S();
        syncManager.syncValue("auto_output", autoOutputValue);
        mainWidget.child(WirelessSteamUI.create(syncManager, binding, guiData.getPlayer())
                .child(new ToggleButton()
                        .syncHandler("auto_output")
                        .tooltipDynamic(t -> t.addLine(Component.translatable(
                                autoOutputValue.getBoolValue() ? AUTO_OUTPUT_ON_KEY : AUTO_OUTPUT_OFF_KEY)))
                        .overlay(false, GTGuiTextures.BUTTON_FLUID_OUTPUT)
                        .overlay(true, GTGuiTextures.BUTTON_FLUID_OUTPUT)
                        .background(GuiTextures.MC_BUTTON)
                        .background(true, GuiTextures.MC_BUTTON_PRESSED)
                        .horizontalCenter()));
    }
}
