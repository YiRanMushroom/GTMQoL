package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTEmiRecipe;
import com.yiran.minecraft.gtmqol.common.blockprovider.BlockProviderRecipeCapability;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.common.stacklike.StackLikeEmi;

import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * gtceu's EMI recipes only list items and fluids as inputs and outputs, so EMI can't find a recipe from anything else.
 * Adds our recipe capabilities: {@link BlockProviderRecipeCapability} blocks as their items and
 * {@link GhostIngredientRecipeCapability} marks, both with chance 0 like gtceu's non-consumed inputs (encoding a
 * pattern leaves them out; a mark is set in the hatch, not sent by the pattern), and the stack-like inputs and outputs
 * (see {@link StackLikeEmi}).
 */
@Mixin(value = GTEmiRecipe.class, remap = false)
public abstract class GTEmiRecipeIngredientsMixin {

    @Shadow
    @Final
    GTRecipe recipe;

    @ModifyReturnValue(method = "getInputs", at = @At("RETURN"))
    private List<EmiIngredient> gtmqol$addInputs(List<EmiIngredient> inputs) {
        var blocks = BlockProviderRecipeCapability.CAP;
        for (var content : recipe.getInputContents(blocks)) {
            var stack = EmiStack.of(blocks.of(content.content()).asItem());
            if (!stack.isEmpty()) inputs.add(stack.setChance(0));
        }
        var marks = GhostIngredientRecipeCapability.CAP;
        for (var content : recipe.getInputContents(marks)) {
            EmiIngredient emi = gtmqol$toEmi(marks.of(content.content()));
            if (!emi.isEmpty()) inputs.add(emi);
        }
        StackLikeEmi.addInputs(recipe, inputs);
        return inputs;
    }

    @ModifyReturnValue(method = "getOutputs", at = @At("RETURN"))
    private List<EmiStack> gtmqol$addOutputs(List<EmiStack> outputs) {
        StackLikeEmi.addOutputs(recipe, outputs);
        return outputs;
    }

    @Unique
    private static EmiIngredient gtmqol$toEmi(GhostIngredient<?> ghost) {
        return gtmqol$toEmi(ghost.toEntryList());
    }

    @Unique
    private static <E> EmiIngredient gtmqol$toEmi(EntryList<E> entries) {
        return EmiStackConverter.convertToEmiEntry(entries, 0, UnaryOperator.identity());
    }
}
