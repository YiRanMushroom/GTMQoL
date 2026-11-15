package com.yiran.minecraft.gtmqol.integration.ae2.datacenter;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import appeng.api.networking.IManagedGridNode;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * The ME Data Center on the ME network: only its grid node. The crafting CPUs are the controller's, which use this
 * node; the network's crafting service finds them through this machine ({@link MEDataCenterHooks#addCpus}).
 */
public class MEDataCenterInterfaceMachine extends MultiblockPartMachine implements IGridConnectedMachine, IMuiMachine {

    @SaveField
    private final GridNodeHolder nodeHolder;
    /**
     * The data center this last formed with. Kept while the structure is broken, so its running jobs stay on the
     * network, paused, instead of being dropped by it.
     */
    private @Nullable MEDataCenterMachine dataCenter;
    private boolean online;

    public MEDataCenterInterfaceMachine(BlockEntityCreationInfo info) {
        super(info);
        this.nodeHolder = attachTrait(new GridNodeHolder(this));
    }

    /** Like GTCEu's ME hatches, the network connects to the front only. */
    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.of(newFacing));
    }

    @Nullable
    MEDataCenterMachine dataCenter() {
        return dataCenter;
    }

    void setDataCenter(@Nullable MEDataCenterMachine dataCenter) {
        this.dataCenter = dataCenter;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (dataCenter != null) dataCenter.interfaceGone(this);
        dataCenter = null;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var connected = new BooleanSyncValue(() -> dataCenter != null && dataCenter.isFormed());
        var online = new BooleanSyncValue(this::isOnline);
        syncManager.syncValue("connected", connected);
        syncManager.syncValue("online", online);
        mainWidget.child(Flow.col()
                .coverChildren()
                .childPadding(4)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .center()
                .child(new TextWidget<>(Text.dynamic(() -> !connected.getBoolValue() ?
                        Component.translatable(MEDataCenterMachine.KEY + "interface.no_data_center")
                                .withStyle(ChatFormatting.RED) :
                        online.getBoolValue() ? Component.translatable("gtceu.gui.me_network.online") :
                                Component.translatable("gtceu.gui.me_network.offline")))));
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }
}
