package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientHandler;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientHatchPartMachine;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredients;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;
import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.Nullable;

/**
 * Gregified single block: a {@link SimpleTieredMachine} with a batch mode toggle, and when its recipes take ghost
 * ingredients, its own list of marks (like a Ghost Ingredient Hatch, which single blocks can't have), opened from a
 * button on the left.
 */
public class GregifiedTieredMachine extends SimpleTieredMachine {

    private final @Nullable GhostIngredientHandler ghostMarks;

    public GregifiedTieredMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        attachPersistentTrait("batchMode", new BatchModeTrait());
        if (takesGhostIngredients()) {
            ghostMarks = new GhostIngredientHandler();
            attachPersistentTrait("ghostMarks", ghostMarks);
        } else {
            ghostMarks = null;
        }
    }

    private boolean takesGhostIngredients() {
        for (GTRecipeType type : getDefinition().getRecipeTypes()) {
            if (type.getMaxInputs(GhostIngredientRecipeCapability.CAP) > 0) return true;
        }
        return false;
    }

    /** The button for the marks, if it has them; from {@code GTSingleblockMachinePanelsMixin}. */
    public MachineUIPanelBuilder addGhostMarksButton(MachineUIPanelBuilder builder, PanelSyncManager syncManager) {
        GhostIngredientHandler marks = ghostMarks;
        if (marks == null) return builder;
        IPanelHandler panel = syncManager.syncedPanel("gtmqol_ghost_marks", true,
                (panelSyncManager, handler) -> PopupPanel.createPopupPanel("gtmqol_ghost_marks_panel",
                                GhostIngredientHandler.LIST_WIDTH + 8, GhostIngredientHandler.LIST_HEIGHT + 8)
                        .child(marks.buildList(panelSyncManager).margin(4)));
        var left = builder.leftConfigurators();
        return builder.leftConfigurators(left.andThen(f -> f.child(new ButtonWidget<>()
                .size(18)
                .onMousePressed((context, button) -> {
                    if (button != InputConstants.MOUSE_BUTTON_LEFT) return false;
                    panel.openPanel();
                    return true;
                })
                .overlay(new ItemDrawable(GhostIngredients.GHOST_INGREDIENT_HATCH.asStack()).asIcon().size(16))
                .tooltip(new RichTooltip().addLine(Text.lang(GhostIngredientHatchPartMachine.KEY + "title"))))));
    }
}
