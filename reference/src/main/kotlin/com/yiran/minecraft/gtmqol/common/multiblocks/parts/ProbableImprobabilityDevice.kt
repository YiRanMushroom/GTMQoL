package com.yiran.minecraft.gtmqol.common.multiblocks.parts

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.yiran.minecraft.gtmqol.api.AbstractRecipeModifierPartMachine
import com.yiran.minecraft.gtmqol.logic.RecipeUtils.copyMutableFrom
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.valueproviders.ConstantInt

class ProbableImprobabilityDevice(holder: IMachineBlockEntity) : AbstractRecipeModifierPartMachine(holder) {
    override fun getRecipeModifier(): ModifierFunction {
        return ModifierFunction(this::finishedRecipeSupplier)
    }

    fun modifyInputContent(value: Content): Content {
        return if (value.chance != ChanceLogic.getMaxChancedValue()) {
            Content(modifyRange(value.content), 0, ChanceLogic.getMaxChancedValue(), 0)
        } else {
            Content(modifyRange(value.content), value.chance, value.maxChance, value.tierChanceBoost)
        }
    }

    private fun modifyRange(value: Any): Any {
        return when (value) {
            is IntProviderIngredient -> IntProviderIngredient.of(
                value.inner,
                ConstantInt.of(value.countProvider.minValue)
            )

            is IntProviderFluidIngredient -> IntProviderFluidIngredient.of(
                value.inner,
                ConstantInt.of(value.countProvider.minValue)
            )

            else -> value
        }
    }

    private val finishedRecipeCache = mutableMapOf<ResourceLocation, GTRecipe>()

    // add a cache for calculated recipes
    fun finishedRecipeSupplier(recipe: GTRecipe): GTRecipe {
        return finishedRecipeCache.getOrPut(recipe.id) {
            GTRecipe(
                recipe.recipeType,
                recipe.id.withSuffix("_generated_probable_improbability"),
                recipe.inputs.mapValues { (_, value) ->
                    value.map(this::modifyInputContent)
                },
                recipe.outputs,
                recipe.tickInputs.mapValues { (_, value) ->
                    value.map(this::modifyInputContent)
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