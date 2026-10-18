package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe.GTRecipeJEICategory;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * gtceu's JEI recipes only list items and fluids for lookup; add {@link GhostIngredientRecipeCapability} marks
 * (invisible catalysts; the recipe shows them in its own slots), so JEI finds the recipe from the marked thing. JEI
 * has to know the value class as an ingredient type (item and fluid stacks).
 */
@Mixin(value = GTRecipeJEICategory.class, remap = false)
public class GTRecipeJEICategoryGhostIngredientMixin {

    // Full descriptor, not the generic bridge (Object recipe).
    @Inject(method = "setupRecipeIngredients(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("TAIL"))
    private void gtmqol$addGhostIngredients(IRecipeLayoutBuilder builder, GTRecipe recipe, IFocusGroup focuses,
                                            CallbackInfo ci) {
        var cap = GhostIngredientRecipeCapability.CAP;
        for (var content : recipe.getInputContents(cap)) {
            builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                    .addIngredientsUnsafe(List.of(cap.of(content.content()).value()));
        }
    }
}
