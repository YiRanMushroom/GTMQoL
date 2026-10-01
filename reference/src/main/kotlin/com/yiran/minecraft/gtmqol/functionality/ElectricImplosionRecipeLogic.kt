package com.yiran.minecraft.gtmqol.functionality

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import com.yiran.minecraft.gtmqol.data.QoLRecipeTypes
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient
import java.util.function.Consumer

object ElectricImplosionRecipeLogic {
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun onImplosionRecipeBuild(recipeBuilder: GTRecipeBuilder, provider: Consumer<FinishedRecipe>) {
        if (!ConfigHolder.instance.addonConfig.enableElectricImplosionRecipes) return

        val itemInputs = recipeBuilder.input[ItemRecipeCapability.CAP] ?: return
        val industrialTnt = GTBlocks.INDUSTRIAL_TNT.asStack()

        val hasTnt = itemInputs.any { content ->
            val ingredient = ItemRecipeCapability.CAP.of(content.content)
            if (ingredient is Ingredient) ingredient.test(industrialTnt) else false
        }

        if (hasTnt) {
            val electricId = ResourceLocation.tryBuild(recipeBuilder.id.namespace, "${recipeBuilder.id.path.removeSuffix("_itnt")}_electric")!!
            val electricBuilder = QoLRecipeTypes.ELECTRIC_IMPLOSION_RECIPES!!.recipeBuilder(electricId)

            electricBuilder.duration(recipeBuilder.duration * 4)

            val originalEnergy = recipeBuilder.EUt()
            if (originalEnergy != EnergyStack.EMPTY) {
                electricBuilder.EUt(originalEnergy.voltage(), originalEnergy.amperage())
            }

            recipeBuilder.input.forEach { (capability, contents) ->
                val cap = capability as RecipeCapability<Any>
                contents.forEach inner@{ content ->
                    val ingredient = cap.of(content.content)

                    if (cap == ItemRecipeCapability.CAP && ingredient is Ingredient && ingredient.test(industrialTnt)) {
                        return@inner
                    }

                    electricBuilder.chance(content.chance)
                        .maxChance(content.maxChance)
                        .tierChanceBoost(content.tierChanceBoost)
                        .input(cap, ingredient)
                }
            }

            recipeBuilder.output.forEach { (capability, contents) ->
                val cap = capability as RecipeCapability<Any>
                contents.forEach { content ->
                    electricBuilder.chance(content.chance)
                        .maxChance(content.maxChance)
                        .tierChanceBoost(content.tierChanceBoost)
                        .output(cap, cap.of(content.content))
                }
            }

            electricBuilder.data.merge(recipeBuilder.data)
            electricBuilder.conditions.addAll(recipeBuilder.conditions)
            electricBuilder.save(provider)
        }
    }
}