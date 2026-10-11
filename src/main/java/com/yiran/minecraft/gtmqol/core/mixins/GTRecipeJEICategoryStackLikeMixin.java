package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe.GTRecipeJEICategory;
import com.yiran.minecraft.gtmqol.common.stacklike.StackLikeJei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** gtceu's JEI recipes only list items and fluids for lookup; add the stack-like ones (see {@link StackLikeJei}). */
@Mixin(value = GTRecipeJEICategory.class, remap = false)
public class GTRecipeJEICategoryStackLikeMixin {

    // Full descriptor, not the generic bridge (Object recipe).
    @Inject(method = "setupRecipeIngredients(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("TAIL"))
    private void gtmqol$addStackLikeIngredients(IRecipeLayoutBuilder builder, GTRecipe recipe, IFocusGroup focuses,
                                                CallbackInfo ci) {
        StackLikeJei.addIngredients(builder, recipe);
    }
}
