package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.yiran.minecraft.gtmqol.common.overclock.Overclocking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

/**
 * gtceu bug: {@code ParallelLogic} keeps the multiplied output amounts within int ({@code limitMaxParallelByOutput}),
 * but not the inputs, the voided outputs or the total runs. An input stock above 2^31 (an ME network) allowed parallels
 * whose input amounts saturated at int, so a run took less than its crafts needed (2 per craft: about half was free).
 * And as chanced outputs aren't multiplied, nothing kept the parallels of the hatch, subtick overclocking and batch
 * mode together below 2^31 when every output was chanced or voided ({@code GTRecipeMixin}). Every parallel limit
 * asked for now takes only the room left ({@link Overclocking#room}), for gtceu's modifiers and ours alike.
 * Remove once gtceu fixes it.
 */
// Untested: a multiblock fed from an ME network with over 2.1 billion of an input it uses 2 or more of per craft, with
// parallels high enough to need more than 2.1 billion of it per run, takes the full amount (before: at most 2.1 billion)
@Mixin(value = ParallelLogic.class, remap = false)
public class ParallelLogicMixin {

    @ModifyVariable(method = { "getParallelAmount", "getParallelAmountWithoutEU", "getParallelAmountFast" },
                    at = @At("HEAD"), argsOnly = true)
    private static int gtmqol$withinRoom(int parallelLimit, @Local(argsOnly = true) GTRecipe recipe) {
        return Math.min(parallelLimit, Overclocking.room(recipe));
    }
}
