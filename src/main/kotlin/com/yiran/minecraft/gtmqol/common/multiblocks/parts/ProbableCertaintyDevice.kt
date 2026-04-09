package com.yiran.minecraft.gtmqol.common.multiblocks.parts

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.yiran.minecraft.gtmqol.api.AbstractRecipeModifierPartMachine
import net.minecraft.util.valueproviders.ConstantInt

class ProbableCertaintyDevice(holder: IMachineBlockEntity) : AbstractRecipeModifierPartMachine(holder) {
    override fun getRecipeModifier(): ModifierFunction {
        return ModifierFunction(ProbableCertaintyDevice::finishedRecipeSupplier)
    }

    companion object {
        fun modifyOutputContent(value: Content): Content {
            return Content(modifyRange(value), ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0)
        }

        private fun modifyRange(value: Any): Any {
            return when (value) {
                is IntProviderIngredient -> IntProviderIngredient.of(value.inner, ConstantInt.of(value.countProvider.maxValue))
                is IntProviderFluidIngredient -> IntProviderFluidIngredient.of(value.inner, ConstantInt.of(value.countProvider.maxValue))
                else -> value
            }
        }

        private val finishedRecipeCache = mutableMapOf<GTRecipe, GTRecipe>()

        // add a cache for calculated recipes
        fun finishedRecipeSupplier(recipe: GTRecipe): GTRecipe {
            return finishedRecipeCache.getOrPut(recipe) {
                GTRecipe(
                    recipe.recipeType,
                    null,
                    recipe.inputs,
                    recipe.outputs.mapValues { (_, value) ->
                        value.map(ProbableCertaintyDevice::modifyOutputContent)
                    },
                    recipe.tickInputs,
                    recipe.tickOutputs.mapValues { (_, value) ->
                        value.map(ProbableCertaintyDevice::modifyOutputContent)
                    },
                    recipe.inputChanceLogics,
                    recipe.outputChanceLogics.mapValues { ChanceLogic.OR },
                    recipe.tickInputChanceLogics,
                    recipe.tickOutputChanceLogics.mapValues { ChanceLogic.OR },
                    recipe.conditions,
                    recipe.ingredientActions,
                    recipe.data,
                    recipe.duration,
                    recipe.recipeCategory,
                    recipe.groupColor
                )
            }
        }
    }
}