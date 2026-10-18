package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.gui.CapabilityContentBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;

import net.minecraft.network.chat.Component;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;

/**
 * Recipe viewer display of {@link GhostIngredientRecipeCapability} inputs, in the input column, as catalysts.
 * Recipe types need {@code setMaxSize(IO.IN, CAP, n)} for slots.
 *
 * <p>One capability holds items and fluids, but a recipe viewer slot has one stack class, fixed when it is made, and
 * the layout is made before the recipe is known. So the layout places empty 18 × 18 holders, and the content builder
 * (run before the widgets are initialised) puts a slot of the right class into each.</p>
 */
public final class GhostIngredientRecipeViewer {

    public static final String TOOLTIP_KEY = "gtmqol.recipe.ghost_ingredient";

    private static final GhostIngredientRecipeCapability CAP = GhostIngredientRecipeCapability.CAP;

    private GhostIngredientRecipeViewer() {}

    /** Called for every recipe type UI layout; a recipe type's own {@code UI(...)} can still override these. */
    public static void addDefaults(GTRecipeTypeUILayout.Builder builder) {
        builder.setRecipeViewerLayoutCapabilityLayoutBuilder(CAP, LAYOUT);
        builder.setCapabilityContentBuilder(CAP, CONTENT);
    }

    private static final RecipeViewerCapabilityLayoutBuilder LAYOUT = (layout, widget, io) -> {
        int slots = layout.getRecipeType().getMaxSlots(CAP, io);
        if (slots == 0) return;
        var column = io == IO.IN ? widget.inputColumn : widget.outputColumn;
        if (slots == 1) {
            column.child(holder(io, 0));
            return;
        }
        column.child(SlotGroupWidget.builder()
                .matrix(layout.capabilityInfo(CAP).getRecipeViewerGrid(io))
                .key('s', i -> holder(io, i))
                .build()
                .coverChildren(18, 18));
    };

    private static ParentWidget<?> holder(IO io, int i) {
        return new ParentWidget<>().size(18).name(GTRecipeViewerWidget.capabilityWidgetName(CAP, io, i));
    }

    private static final CapabilityContentBuilder CONTENT = (widget, content, io, perTick, recipeType, recipe,
                                                             chanceTier, recipeTier) -> {
        if (!(widget instanceof ParentWidget<?> holder)) return;
        var slot = slot(CAP.of(content.content()));
        slot.tooltipBuilder(tooltip -> tooltip.addLine(Component.translatable(TOOLTIP_KEY)));
        // never consumed
        slot.recipeSlotRole(RecipeSlotRole.CATALYST);
        holder.child(slot);
    };

    private static <T> RecipeViewerSlotWidget<?, ?> slot(GhostIngredient<T> ghost) {
        EntryList<T> entries = ghost.toEntryList();
        return RecipeViewerSlotWidget.create(entries.getType())
                .value(entries)
                .background(ghost.slotBackground());
    }
}
