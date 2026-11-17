package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.yiran.minecraft.gtmqol.common.overclock.Overclocking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

/**
 * gtceu bug: {@code getTotalRuns} multiplies parallels, subtick parallels and batch parallels as ints, so above 2^31
 * crafts it wraps to a negative number: the chance rolls of a negative number of runs give no chanced outputs, and
 * the machine window shows negative total runs. Saturate at {@link Overclocking#MAX_PARALLELS} instead: a fallback,
 * as {@code ParallelLogicMixin} keeps the product below it ({@link Overclocking#room}). Remove once gtceu fixes it.
 */
// Untested: a batch-mode multiblock with a huge parallel hatch and subtick parallels (together above 2.1 billion
// crafts before) keeps its chanced outputs and shows positive total runs, at most 2,000,000,000
@Mixin(value = GTRecipe.class, remap = false)
public abstract class GTRecipeMixin {

    @ModifyReturnValue(method = "getTotalRuns", at = @At("RETURN"))
    private int gtmqol$saturate(int wrapped) {
        return Overclocking.crafts((GTRecipe) (Object) this);
    }
}
