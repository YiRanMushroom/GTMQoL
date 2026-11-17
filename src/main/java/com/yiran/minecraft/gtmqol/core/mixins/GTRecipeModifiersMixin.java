package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.yiran.minecraft.gtmqol.common.overclock.Overclocking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

/**
 * The multi smelter runs base -> overclock -> parallel. With {@code OverclockingLogicMixin} the overclock itself
 * computes parallels from a single smelt, which is wrong once the smelter's own parallels are multiplied in after
 * it. Not optional for that reason: drop the overclock from the middle and apply it last, to the parallelized
 * recipe.
 * <p>
 * Batch mode is limited so that the batch times the recipe's other parallels stays within
 * {@link Overclocking#MAX_PARALLELS} ({@link Overclocking#room}): above 2^31 crafts gtceu's int total runs went
 * negative and the chanced outputs were lost.
 */
@Mixin(value = GTRecipeModifiers.class, remap = false)
public class GTRecipeModifiersMixin {

    @WrapOperation(method = "multiSmelterParallel",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;getModifier(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;J)Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;"))
    private static ModifierFunction gtmqol$skipOverclock(OverclockingLogic logic, MetaMachine machine,
                                                         GTRecipe recipe, long maxVoltage,
                                                         Operation<ModifierFunction> original) {
        return ModifierFunction.IDENTITY;
    }

    @WrapOperation(method = "batchMode",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/recipe/modifier/ParallelLogic;getParallelAmountWithoutEU(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;I)I"))
    private static int gtmqol$batchWithinRoom(MetaMachine machine, GTRecipe recipe, int limit,
                                              Operation<Integer> original) {
        return original.call(machine, recipe, Math.min(limit, Overclocking.room(recipe)));
    }

    @ModifyReturnValue(method = "multiSmelterParallel", at = @At("RETURN"))
    private static ModifierFunction gtmqol$overclockLast(ModifierFunction original,
                                                         @Local(argsOnly = true) MetaMachine machine,
                                                         @Local(argsOnly = true) GTRecipe recipe) {
        if (original == ModifierFunction.NULL ||
                !(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return original;
        }
        GTRecipe parallelized = original.apply(recipe);
        if (parallelized == null) return original;
        return original.andThen(OverclockingLogic.NON_PERFECT_OVERCLOCK.getModifier(machine, parallelized,
                coilMachine.getOverclockVoltage()));
    }
}
