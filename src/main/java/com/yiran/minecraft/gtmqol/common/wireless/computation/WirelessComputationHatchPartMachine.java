package com.yiran.minecraft.gtmqol.common.wireless.computation;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
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
 * gtceu's computation data hatch over a channel instead of an optical cable: the transmitter goes on an HPCA or
 * network switch, the receiver on a research station or network switch.
 */
public class WirelessComputationHatchPartMachine extends MultiblockPartMachine implements IMuiMachine {

    @SaveField
    @SyncToClient
    public final ChannelBindingTrait binding;

    public final WirelessComputationContainer container;

    public WirelessComputationHatchPartMachine(BlockEntityCreationInfo info, boolean transmitter) {
        super(info);
        this.binding = attachTrait(new ChannelBindingTrait(ChannelKind.COMPUTATION,
                transmitter ? ChannelRole.TRANSMITTER : ChannelRole.RECEIVER));
        this.container = attachTrait(new WirelessComputationContainer(binding, transmitter));
    }

    @Override
    public boolean canShared(MultiblockControllerMachine controller, String substructureName) {
        return false;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(ChannelUI.create(syncManager, binding, guiData.getPlayer()));
    }
}
