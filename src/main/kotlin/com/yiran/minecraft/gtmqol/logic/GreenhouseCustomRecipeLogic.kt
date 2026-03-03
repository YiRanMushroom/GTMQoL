package com.yiran.minecraft.gtmqol.logic

import com.gregtechceu.gtceu.api.GTValues.*
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.GTRecipeType.ICustomRecipeLogic
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities
import com.yiran.minecraft.gtmqol.data.QoLRecipeTypes
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.registries.ForgeRegistries


class GreenhouseCustomRecipeLogic : ICustomRecipeLogic {
    val itemToRecipeCache: MutableMap<Item, GTRecipe?> = mutableMapOf()

    fun handleSaplingRecipes(location: ResourceLocation): GTRecipe? {
        if (!location.path.endsWith("_sapling")) return null
        val saplingItem = ForgeRegistries.ITEMS.getValue(location) ?: return null
        val logOrStemItem = ForgeRegistries.ITEMS.getValue(
            ResourceLocation.tryBuild(location.namespace, location.path.removeSuffix("_sapling") + "_log")
                ?: ResourceLocation.tryBuild(location.namespace, location.path.removeSuffix("_sapling") + "_stem")
        ) ?: return null
        val leavesItem = ForgeRegistries.ITEMS.getValue(
            ResourceLocation.tryBuild(
                location.namespace,
                location.path.removeSuffix("_sapling") + "_leaves"
            )
        )

        val stickItem = Items.STICK

        itemToRecipeCache[saplingItem] =
            QoLRecipeTypes.GREENHOUSE_RECIPES!!.recipeBuilder("grow_${location.namespace}_${location.path}")
                .notConsumable(saplingItem)
                .notConsumableFluid(FluidStack(Fluids.WATER, 1000))
                .outputItems(saplingItem, 2)
                .outputItems(logOrStemItem, 4)
                .outputItems(stickItem, 4)
                .apply {
                    leavesItem?.let { outputItems(it, 4) }
                }
                .duration(600)
                .EUt(VA[LV].toLong())
                .buildRawRecipe()

        return itemToRecipeCache[saplingItem]
    }

    fun handleSeedRecipes(resourceLocation: ResourceLocation): GTRecipe? {
        return null
    }

    fun handleCropRecipes(resourceLocation: ResourceLocation): GTRecipe? {
        return null
    }

    override fun createCustomRecipe(recipeCapabilityHolder: IRecipeCapabilityHolder): GTRecipe? {
        val itemIn = recipeCapabilityHolder.capabilitiesFlat.get(IO.IN)?.get(GTRecipeCapabilities.ITEM) ?: return null

        itemIn.forEach { recipeHandler ->
            recipeHandler.contents.forEach { itemStack ->
                if (itemStack is ItemStack) {
                    itemToRecipeCache.get(itemStack.item)?.let {
                        return it
                    }

                    val location = ForgeRegistries.ITEMS.getKey(itemStack.item) ?: return null

                    return handleSaplingRecipes(location) ?: handleSeedRecipes(location) ?: handleCropRecipes(location)
                }
            }
        }

        return null
    }
}