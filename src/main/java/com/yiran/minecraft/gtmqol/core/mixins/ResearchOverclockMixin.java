package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Research recipes ({@code duration_is_total_cwu}: the duration is the total CWU, and the station advances by the
 * CWU it draws each tick) ignore the duration multiplier: gtceu takes 2.5% off the total per overclock while EU/t
 * still goes up 4x, so more voltage only costs power. Here each overclock halves the total CWU instead. The recipe
 * viewer's OC preview builds through the same lambda.
 */
// Untested: a research station one tier above the recipe shows and takes half the total CWU at 4x EU/t
@Mixin(value = ModifierFunction.FunctionBuilder.class, remap = false)
public class ResearchOverclockMixin {

    @Shadow
    private int addOCs;

    @WrapOperation(method = "lambda$build$1",
                   at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"),
                   require = 1)
    private float gtmqol$halveTotalCwuPerOverclock(float min, float scaled, Operation<Float> original,
                                                   @Local(argsOnly = true) GTRecipe recipe) {
        return original.call(min, (float) (recipe.duration * Math.pow(0.5, addOCs)));
    }
}
