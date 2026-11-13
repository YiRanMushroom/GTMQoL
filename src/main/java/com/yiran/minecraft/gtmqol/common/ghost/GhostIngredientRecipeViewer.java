package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.gui.CapabilityContentBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;
import com.yiran.minecraft.gtmqol.common.ui.RecipeViewerWidgetAccess;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.widget.ParentWidget;

/**
 * Recipe viewer display of {@link GhostIngredientRecipeCapability} inputs: as catalysts, above the progress bar, one
 * slot per ghost ingredient the recipe has, faded like a ghost. Recipe types need
 * {@code setMaxSize(IO.IN, CAP, n)}.
 *
 * <p>One capability holds items and fluids, but a recipe viewer slot has one stack class, fixed when it is made, and
 * the layout is made before the recipe is known. So the layout places empty 18 × 18 holders, and the content builder
 * (run before the widgets are initialised) puts a slot of the right class into each.</p>
 */
public final class GhostIngredientRecipeViewer {

    public static final String TOOLTIP_KEY = "gtmqol.recipe.ghost_ingredient";

    private static final GhostIngredientRecipeCapability CAP = GhostIngredientRecipeCapability.CAP;

    private GhostIngredientRecipeViewer() {}

    /** Called for every recipe type UI layout: fills in what the recipe type didn't set itself for this capability. */
    public static void addDefaults(GTRecipeTypeUILayout layout) {
        var info = layout.capabilityInfo(CAP);
        if (info.recipeViewerLayoutBuilder == null) info.recipeViewerLayoutBuilder = LAYOUT;
        if (info.capabilityWidgetBuilder == null) info.capabilityWidgetBuilder = CONTENT;
    }

    /** Inputs above the progress bar ({@link RecipeViewerWidgetAccess}); recipes without one get no empty slot. */
    // Untested: a recipe with a ghost ingredient (Create saw, MI assembler, Advanced Chip Engraving Factory) shows it
    // above the progress bar, faded and slowly pulsing, with its tooltip on hover; recipes without one look as before
    private static final RecipeViewerCapabilityLayoutBuilder LAYOUT = (layout, widget, io) -> {
        if (layout.getRecipeType().getMaxSlots(CAP, io) == 0) return;
        var access = (RecipeViewerWidgetAccess) widget;
        var recipe = access.gtmqol$recipe();
        int count = (io == IO.IN ? recipe.getInputContents(CAP) : recipe.getOutputContents(CAP)).size();
        var parent = io == IO.IN ? access.gtmqol$ghostSlots() : widget.outputColumn;
        for (int i = 0; i < count; i++) parent.child(holder(io, i));
    };

    /** Over the slot's stack: fades it, breathing slowly, so it reads as a mark rather than an ingredient. */
    private static final IDrawable GHOST_OVERLAY = (context, x, y, width, height, theme) -> {
        double phase = System.currentTimeMillis() % 2400 / 2400.0 * 2 * Math.PI;
        int alpha = (int) (0x78 + 0x20 * Math.sin(phase));
        GuiDraw.drawRect(context.getGraphics(), x + 1, y + 1, width - 2, height - 2, alpha << 24 | 0x8B8B8B);
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
        slot.overlay(GHOST_OVERLAY);
        holder.child(slot);
    };

    private static RecipeViewerSlotWidget<?, ?> slot(GhostIngredient<?> ghost) {
        return slot(ghost.toEntryList()).background(ghost.slotBackground());
    }

    private static <E> RecipeViewerSlotWidget<?, ?> slot(EntryList<E> entries) {
        return RecipeViewerSlotWidget.create(entries.getType()).value(entries);
    }
}
