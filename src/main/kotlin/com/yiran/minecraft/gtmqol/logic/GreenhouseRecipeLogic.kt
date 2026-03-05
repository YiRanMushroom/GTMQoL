package com.yiran.minecraft.gtmqol.logic

import com.gregtechceu.gtceu.api.GTValues.LV
import com.gregtechceu.gtceu.api.GTValues.VA
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.yiran.minecraft.gtmqol.data.QoLRecipeTypes
import com.yiran.minecraft.gtmqol.logic.RegistryUtils.addSuffix
import com.yiran.minecraft.gtmqol.logic.RegistryUtils.asItem
import com.yiran.minecraft.gtmqol.logic.RegistryUtils.atNamespace
import com.yiran.minecraft.gtmqol.logic.RegistryUtils.resourceLocation
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

object GreenhouseRecipeLogic {
    private val saplingProviders = mutableListOf<Supplier<Item?>>()

    @JvmStatic
    fun addSaplingProvider(provider: Supplier<Item?>) {
        saplingProviders.add(provider)
    }

    fun init(provider: Consumer<FinishedRecipe>) {
        saplingProviders.mapNotNull(Supplier<Item?>::get).forEach {
            for (recipeProvider in TreeGrowingRecipeProviders) {
                val recipeBuilder = recipeProvider(it) ?: continue
                recipeBuilder.save(provider)
            }
        }
    }

    val TreeGrowingRecipeProviders: LinkedList<(Item) -> GTRecipeBuilder?> = LinkedList()

    init {
        TreeGrowingRecipeProviders.add(::defaultTreeGrowingRecipeProvider)
    }

    fun defaultTreeGrowingRecipeProvider(item: Item): GTRecipeBuilder? {
        if (item !is BlockItem || item.block !is SaplingBlock) {
            return null
        }

        val location = item.resourceLocation()!!

        val namespace = location.namespace
        val path = location.path

        val logItem = path.substringBeforeLast('_').addSuffix("_log").atNamespace(namespace).asItem() ?: return null
        val leavesItem =
            path.substringBeforeLast('_').addSuffix("_leaves").atNamespace(namespace).asItem() ?: return null

        return QoLRecipeTypes.GREENHOUSE_RECIPES!!.recipeBuilder("gtmqol:greenhouse/grow_${namespace}_${path}")
            .notConsumable(item)
            .notConsumableFluid(FluidStack(Fluids.WATER, 1000))
            .circuitMeta(1)
            .EUt(VA[LV].toLong())
            .outputItems(item, 4)
            .outputItems(logItem, 8)
            .outputItems(leavesItem, 4)
    }
}