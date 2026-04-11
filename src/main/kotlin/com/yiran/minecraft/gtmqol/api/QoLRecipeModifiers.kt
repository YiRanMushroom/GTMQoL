package com.yiran.minecraft.gtmqol.api

import com.gregtechceu.gtceu.api.capability.IParallelHatch
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import com.yiran.minecraft.gtmqol.ModUtils.asNotNull


object QoLRecipeModifiers {
    @JvmStatic
    fun absoluteHatchParallel(machine: MetaMachine, recipe: GTRecipe): ModifierFunction {
        if (machine is MultiblockControllerMachine && machine.isFormed()) {
            val parallels = machine.parallelHatch
                .map { hatch: IParallelHatch ->
                    ParallelLogic.getParallelAmount(
                        machine,
                        recipe,
                        hatch.currentParallel
                    )
                }
                .orElse(1)
                .asNotNull()

            if (parallels == 1) return ModifierFunction.IDENTITY
            return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels.toDouble()))
                .parallels(parallels)
                .build()
        }
        return ModifierFunction.IDENTITY
    }

    @JvmField
    public val ABSOLUTE_HATCH_PARALLEL: RecipeModifier = RecipeModifier(::absoluteHatchParallel)
}