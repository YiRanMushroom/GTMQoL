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

class ProbableCertaintyDevice(holder: IMachineBlockEntity) : AbstractRecipeModifierPartMachine(holder) {
    override fun getRecipeModifier(): ModifierFunction {
        return ModifierFunction(this::finishedRecipeSupplier)
    }

    fun modifyOutputContent(value: Content): Content {
        return Content(
            modifyRange(value.content),
            ChanceLogic.getMaxChancedValue(),
            ChanceLogic.getMaxChancedValue(),
            0
        )
    }

    private fun modifyRange(value: Any): Any {
        return when (value) {
            is IntProviderIngredient -> IntProviderIngredient.of(
                value.inner,
                ConstantInt.of(value.countProvider.maxValue)
            )

            is IntProviderFluidIngredient -> IntProviderFluidIngredient.of(
                value.inner,
                ConstantInt.of(value.countProvider.maxValue)
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
                recipe.id.withSuffix("_generated_probable_certainty"),
                recipe.inputs,
                recipe.outputs.mapValues { (_, value) ->
                    value.map(this::modifyOutputContent)
                },
                recipe.tickInputs,
                recipe.tickOutputs.mapValues { (_, value) ->
                    value.map(this::modifyOutputContent)
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
            ).copyMutableFrom(recipe)
        }
    }
}