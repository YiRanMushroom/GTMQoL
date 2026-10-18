package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTEmiRecipe;
import com.yiran.minecraft.gtmqol.common.blockprovider.BlockProviderRecipeCapability;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * gtceu's EMI recipes only list items and fluids as inputs; add {@link BlockProviderRecipeCapability} blocks as
 * their items, so EMI finds the recipe from the block. Chance 0 like gtceu's non-consumed inputs, so encoding a
 * pattern leaves them out.
 */
@Mixin(value = GTEmiRecipe.class, remap = false)
public abstract class GTEmiRecipeBlockProviderMixin {

    @Shadow
    @Final
    GTRecipe recipe;

    @ModifyReturnValue(method = "getInputs", at = @At("RETURN"))
    private List<EmiIngredient> gtmqol$addBlockInputs(List<EmiIngredient> inputs) {
        var cap = BlockProviderRecipeCapability.CAP;
        for (var content : recipe.getInputContents(cap)) {
            var stack = EmiStack.of(cap.of(content.content()).asItem());
            if (!stack.isEmpty()) inputs.add(stack.setChance(0));
        }
        return inputs;
    }
}
