package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTEmiRecipe;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;

import brachy.modularui.integration.emi.EmiStackConverter;
import dev.emi.emi.api.stack.EmiIngredient;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * gtceu's EMI recipes only list items and fluids as inputs; add {@link GhostIngredientRecipeCapability} marks, so EMI
 * finds the recipe from the marked thing. Chance 0 like gtceu's non-consumed inputs, so encoding a pattern leaves
 * them out (the mark is set in the hatch, not sent by the pattern).
 */
@Mixin(value = GTEmiRecipe.class, remap = false)
public abstract class GTEmiRecipeGhostIngredientMixin {

    @Shadow
    @Final
    GTRecipe recipe;

    @ModifyReturnValue(method = "getInputs", at = @At("RETURN"))
    private List<EmiIngredient> gtmqol$addGhostIngredientInputs(List<EmiIngredient> inputs) {
        var cap = GhostIngredientRecipeCapability.CAP;
        for (var content : recipe.getInputContents(cap)) {
            EmiIngredient emi = gtmqol$toEmi(cap.of(content.content()));
            if (!emi.isEmpty()) inputs.add(emi);
        }
        return inputs;
    }

    @Unique
    private static <T> EmiIngredient gtmqol$toEmi(GhostIngredient<T> ghost) {
        return EmiStackConverter.convertToEmiEntry(ghost.toEntryList(), 0, UnaryOperator.identity());
    }
}
