package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTEmiRecipe;
import com.yiran.minecraft.gtmqol.common.stacklike.StackLikeEmi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/** gtceu's EMI recipes only list items and fluids as inputs and outputs; add the stack-like ones (see {@link StackLikeEmi}). */
@Mixin(value = GTEmiRecipe.class, remap = false)
public abstract class GTEmiRecipeStackLikeMixin {

    @Shadow
    @Final
    GTRecipe recipe;

    @ModifyReturnValue(method = "getInputs", at = @At("RETURN"))
    private List<EmiIngredient> gtmqol$addStackLikeInputs(List<EmiIngredient> inputs) {
        StackLikeEmi.addInputs(recipe, inputs);
        return inputs;
    }

    @ModifyReturnValue(method = "getOutputs", at = @At("RETURN"))
    private List<EmiStack> gtmqol$addStackLikeOutputs(List<EmiStack> outputs) {
        StackLikeEmi.addOutputs(recipe, outputs);
        return outputs;
    }
}
