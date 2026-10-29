package com.yiran.minecraft.gtmqol.common.wireless.computation;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelBindingTrait;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelKind;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelRole;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelUI;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;

/**
 * Bound to a computation channel, it lets the channel have any number of transmitters and receivers (without one,
 * one of each). Being bound is enough; it doesn't need to be loaded and uses no energy.
 */
public class WirelessComputationRouterMachine extends MetaMachine implements IMuiMachine {

    @SaveField
    @SyncToClient
    public final ChannelBindingTrait binding;

    public WirelessComputationRouterMachine(BlockEntityCreationInfo info) {
        super(info);
        this.binding = attachTrait(new ChannelBindingTrait(ChannelKind.COMPUTATION, ChannelRole.ROUTER));
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(ChannelUI.create(syncManager, binding, guiData.getPlayer()));
    }
}
