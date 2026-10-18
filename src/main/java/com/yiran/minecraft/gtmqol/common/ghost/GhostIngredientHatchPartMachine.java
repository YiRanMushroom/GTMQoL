package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;

/**
 * A list of {@link GhostIngredient}s ({@link GhostIngredientHandler}), which tell the machine what to make; see
 * {@link GhostIngredientRecipeCapability}. Nothing is ever consumed. UI: {@link GhostIngredientListWidget}.
 */
public class GhostIngredientHatchPartMachine extends MultiblockPartMachine implements IMuiMachine {

    public static final String KEY = "gtmqol.machine.ghost_ingredient_hatch.";
    // saved through the field: traits attached without a name aren't
    @SaveField
    private final GhostIngredientHandler handler;

    public GhostIngredientHatchPartMachine(BlockEntityCreationInfo info) {
        super(info);
        this.handler = attachTrait(new GhostIngredientHandler());
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(handler.buildList(syncManager));
    }
}
