package com.yiran.minecraft.gtmqol.common.ui;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import brachy.modularui.widgets.layout.Flow;

/**
 * What {@code GTRecipeViewerWidgetLayoutMixin} adds to gtceu's recipe viewer widget, for capability layouts that need
 * the recipe itself (each recipe's own slot count, {@link DynamicRecipeSlots}) or a place of their own.
 */
public interface RecipeViewerWidgetAccess {

    /** The recipe the widget shows. */
    GTRecipe gtmqol$recipe();

    /** A row above the progress bar, for ghost ingredients; part of the layout only once something is put in it. */
    Flow gtmqol$ghostSlots();
}
