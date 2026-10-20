package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.api.machine.trait.feature.IAttachConfiguratorsTrait;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import com.mojang.blaze3d.platform.InputConstants;

import static com.yiran.minecraft.gtmqol.common.ui.PopupWidgets.popup;

/**
 * Ghost ingredient marks of a single block, which can't have a Ghost Ingredient Hatch: the hatch's list, opened from
 * a button on the left of the machine's UI.
 */
public class GhostMarksTrait extends GhostIngredientHandler implements IAttachConfiguratorsTrait {

    @Override
    public void attachLeftConfigurators(Flow flow, ModularPanel<?> panel, PanelSyncManager syncManager) {
        IPanelHandler marksPanel = syncManager.syncedPanel("gtmqol_ghost_marks", true,
                (panelSyncManager, handler) -> popup("gtmqol_ghost_marks_panel", LIST_WIDTH + 8, LIST_HEIGHT + 8)
                        .child(buildList(panelSyncManager).margin(4)));
        flow.child(new ButtonWidget<>()
                .size(18)
                .onMousePressed((context, button) -> {
                    if (button != InputConstants.MOUSE_BUTTON_LEFT) return false;
                    marksPanel.togglePanel();
                    return true;
                })
                .overlay(new ItemDrawable(GhostIngredients.GHOST_INGREDIENT_HATCH.asStack()).asIcon().size(16))
                .tooltip(new RichTooltip().addLine(Text.lang(GhostIngredientHatchPartMachine.KEY + "title"))));
    }
}
