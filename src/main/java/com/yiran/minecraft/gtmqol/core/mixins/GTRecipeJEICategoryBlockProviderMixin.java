package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe.GTRecipeJEICategory;
import com.yiran.minecraft.gtmqol.common.blockprovider.BlockProviderRecipeCapability;

import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * gtceu's JEI recipes only list items and fluids for lookup; add {@link BlockProviderRecipeCapability} blocks as
 * their items (invisible catalysts; the recipe shows them in its own slot), so JEI finds the recipe from the block.
 */
@Mixin(value = GTRecipeJEICategory.class, remap = false)
public class GTRecipeJEICategoryBlockProviderMixin {

    // Full descriptor, not the generic bridge (Object recipe).
    @Inject(method = "setupRecipeIngredients(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("TAIL"))
    private void gtmqol$addBlockIngredients(IRecipeLayoutBuilder builder, GTRecipe recipe, IFocusGroup focuses,
                                            CallbackInfo ci) {
        var cap = BlockProviderRecipeCapability.CAP;
        for (var content : recipe.getInputContents(cap)) {
            var stack = new ItemStack(cap.of(content.content()).asItem());
            if (!stack.isEmpty()) builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStack(stack);
        }
    }
}
