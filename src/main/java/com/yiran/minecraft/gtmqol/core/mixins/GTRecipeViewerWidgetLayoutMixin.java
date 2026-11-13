package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.yiran.minecraft.gtmqol.common.ui.RecipeViewerWidgetAccess;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.widgets.layout.Flow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * The recipe for capability layouts, and ghost ingredient slots above the progress bar
 * ({@link RecipeViewerWidgetAccess}). Apart from {@code GTRecipeViewerWidgetMixin}, which is switched off with the
 * overclocking overhaul.
 */
@Mixin(value = GTRecipeViewerWidget.class, remap = false)
public abstract class GTRecipeViewerWidgetLayoutMixin implements RecipeViewerWidgetAccess {

    @Shadow
    @Final
    private GTRecipe baseRecipe;

    @Shadow
    @Final
    public Flow inputColumn;

    @Shadow
    @Final
    public Flow outputColumn;

    @Unique
    private final Flow gtmqol$ghostSlots = Flow.row().coverChildren().childPadding(2);

    @Override
    public GTRecipe gtmqol$recipe() {
        return baseRecipe;
    }

    @Override
    public Flow gtmqol$ghostSlots() {
        return gtmqol$ghostSlots;
    }

    /**
     * The capability layouts have run by now. Ghost slots go above the progress bar, between the input and output
     * columns, where they add no width: the recipe viewer is only as wide as gtceu's own layouts need, and a slot
     * outside it can't be hovered.
     */
    @Inject(method = "buildDefaultLayout", at = @At("RETURN"))
    private void gtmqol$placeGhostSlots(CallbackInfoReturnable<Flow> cir) {
        if (gtmqol$ghostSlots.getChildren().isEmpty()) return;
        Flow row = cir.getReturnValue();
        List<IWidget> children = row.getChildren();
        for (int i = 0; i < children.size(); i++) {
            IWidget progressBar = children.get(i);
            if (progressBar == inputColumn || progressBar == outputColumn) continue;
            row.remove(i);
            row.addChild(Flow.col().coverChildren().childPadding(2).child(gtmqol$ghostSlots).child(progressBar), i);
            return;
        }
    }
}
