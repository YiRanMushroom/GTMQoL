package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IAttachConfiguratorsTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;

/**
 * Batch mode toggle for single blocks, the same button multiblocks have. Read by {@link GregificationModifiers#BATCH}.
 */
public class BatchModeTrait extends MachineTrait implements IAttachConfiguratorsTrait {

    @SaveField
    private boolean batchEnabled;

    public boolean isBatchEnabled() {
        return batchEnabled;
    }

    public void setBatchEnabled(boolean batchEnabled) {
        this.batchEnabled = batchEnabled;
        markAsChanged();
    }

    @Override
    public void attachRightConfigurators(Flow flow, ModularPanel<?> panel, PanelSyncManager syncManager) {
        flow.child(GTMuiWidgets.createToggleButton(this::isBatchEnabled, this::setBatchEnabled,
                GTGuiTextures.BUTTON_BATCH[0], GTGuiTextures.BUTTON_BATCH[1], "gtceu.machine.batching"));
    }
}
