package com.yiran.minecraft.gtmqol.common.multiblocks.parts

import com.google.common.base.Functions
import com.google.common.base.Suppliers
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.yiran.minecraft.gtmqol.api.AbstractRecipeModifierPartMachine
import com.yiran.minecraft.gtmqol.logic.RecipeUtils.copyMutableFrom
import net.minecraft.util.valueproviders.ConstantInt

class ProbableImprobabilityDevice(holder: IMachineBlockEntity) : AbstractRecipeModifierPartMachine(holder) {
    override fun getRecipeModifier(): ModifierFunction {
        return ModifierFunction(ProbableImprobabilityDevice::finishedRecipeSupplier)
    }

    companion object {
        fun modifyInputContent(value: Content): Content {
            return if (value.chance != ChanceLogic.getMaxChancedValue()) {
                Content(modifyRange(value.content), 0, ChanceLogic.getMaxChancedValue(), 0)
            } else {
                Content(modifyRange(value.content), value.chance, value.maxChance, value.tierChanceBoost)
            }
        }

        private fun modifyRange(value: Any): Any {
            return when (value) {
                is IntProviderIngredient -> IntProviderIngredient.of(value.inner, ConstantInt.of(value.countProvider.minValue))
                is IntProviderFluidIngredient -> IntProviderFluidIngredient.of(value.inner, ConstantInt.of(value.countProvider.minValue))
                else -> value
            }
        }

        private val finishedRecipeCache = mutableMapOf<GTRecipe, GTRecipe>()

        // add a cache for calculated recipes
        fun finishedRecipeSupplier(recipe: GTRecipe): GTRecipe {
            return finishedRecipeCache.getOrPut(recipe) {
                GTRecipe(
                    recipe.recipeType,
                    recipe.id,
                    recipe.inputs.mapValues { (_, value) ->
                        value.map(ProbableImprobabilityDevice::modifyInputContent)
                    },
                    recipe.outputs,
                    recipe.tickInputs.mapValues { (_, value) ->
                        value.map(ProbableImprobabilityDevice::modifyInputContent)
                    },
                    recipe.tickOutputs,
                    recipe.inputChanceLogics.mapValues { ChanceLogic.OR },
                    recipe.outputChanceLogics,
                    recipe.tickInputChanceLogics.mapValues { ChanceLogic.OR },
                    recipe.tickOutputChanceLogics,
                    recipe.conditions,
                    recipe.ingredientActions,
                    recipe.data,
                    recipe.duration,
                    recipe.recipeCategory,
                    recipe.groupColor
                ).copyMutableFrom(recipe)
            }
        }
    }
}