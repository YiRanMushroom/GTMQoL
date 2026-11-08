package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe.GTRecipeJEICategory;
import com.yiran.minecraft.gtmqol.common.blockprovider.BlockProviderRecipeCapability;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.common.stacklike.StackLikeJei;

import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * gtceu's JEI recipes only list items and fluids for lookup, so JEI can't find a recipe from anything else. Adds our
 * recipe capabilities as invisible catalysts (the recipe shows them in its own slots):
 * {@link BlockProviderRecipeCapability} blocks as their items, {@link GhostIngredientRecipeCapability} marks (JEI has to
 * know the value class as an ingredient type: item and fluid stacks), and the stack-like ones (see {@link StackLikeJei}).
 */
@Mixin(value = GTRecipeJEICategory.class, remap = false)
public class GTRecipeJEICategoryIngredientsMixin {

    // Full descriptor, not the generic bridge (Object recipe).
    @Inject(method = "setupRecipeIngredients(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("TAIL"))
    private void gtmqol$addIngredients(IRecipeLayoutBuilder builder, GTRecipe recipe, IFocusGroup focuses,
                                       CallbackInfo ci) {
        var blocks = BlockProviderRecipeCapability.CAP;
        for (var content : recipe.getInputContents(blocks)) {
            var stack = new ItemStack(blocks.of(content.content()).asItem());
            if (!stack.isEmpty()) builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStack(stack);
        }
        var marks = GhostIngredientRecipeCapability.CAP;
        for (var content : recipe.getInputContents(marks)) {
            builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                    .addIngredientsUnsafe(List.of(marks.of(content.content()).value()));
        }
        StackLikeJei.addIngredients(builder, recipe);
    }
}
