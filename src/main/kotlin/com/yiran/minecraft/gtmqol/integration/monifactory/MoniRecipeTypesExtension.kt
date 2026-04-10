package com.yiran.minecraft.gtmqol.integration.monifactory

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.GTSoundEntries
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture
import com.yiran.minecraft.gtmqol.GTMQoL
import com.yiran.minecraft.gtmqol.ModUtils.asNotNull
import com.yiran.minecraft.gtmqol.ModUtils.asType
import com.yiran.minecraft.gtmqol.logic.RecipeUtils.itemNotConsumableWithName
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.neganote.monilabs.capability.recipe.ChromaIngredient
import net.neganote.monilabs.capability.recipe.MoniRecipeCapabilities
import net.neganote.monilabs.client.gui.MoniGuiTextures
import net.neganote.monilabs.common.data.MoniSounds
import net.neganote.monilabs.common.machine.multiblock.Color
import net.neganote.monilabs.common.machine.multiblock.Microverse
import java.util.function.Consumer


object MoniRecipeTypesExtension {
    @JvmField
    public var OMNI_PRISMATIC_CRUCIBLE_RECIPE: GTRecipeType? = null

    @JvmField
    public var EYE_OF_HARMONY_RECIPE: GTRecipeType? = null

    init {
        if (MoniUtils.isMoniFactoryAndIntegrationEnabled()) {
            OMNI_PRISMATIC_CRUCIBLE_RECIPE =
                GTRecipeTypes
                    .register("omni_prismatic_recipe", GTRecipeTypes.MULTIBLOCK)
                    .setMaxIOSize(6, 6, 3, 3)
                    .setEUIO(IO.IN)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                    .setSound(GTSoundEntries.ELECTROLYZER)

            EYE_OF_HARMONY_RECIPE =
                GTRecipeTypes
                    .register("moni_eye_of_harmony", GTRecipeTypes.MULTIBLOCK)
                    .setEUIO(IO.IN)
                    .setMaxIOSize(9, 9, 3, 0)
                    .setSlotOverlay(false, false, GuiTextures.ARROW_INPUT_OVERLAY)
                    .setProgressBar(MoniGuiTextures.PROGRESS_BAR_ROCKET, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                    .setSound(MoniSounds.MICROVERSE)
        }
    }

    @JvmStatic
    fun init() {
        if (MoniUtils.isMoniFactoryAndIntegrationEnabled()) {
            OMNI_PRISMATIC_CRUCIBLE_RECIPE!!
        }

        GTMQoL.LOGGER.info("Moni Factory recipe types extension initialized")
    }

    @JvmStatic
    fun onPrismaCRecipeBuild(builder: GTRecipeBuilder, provider: Consumer<FinishedRecipe>) {
        val contentList = builder.input[MoniRecipeCapabilities.CHROMA]

        val inputColor = contentList?.firstOrNull()?.let { content ->
            val ingredient = content.content as? ChromaIngredient
            ingredient?.color
        } ?: Color.ANY

        val inputBuilderName = builder.recipeType.registryName

        val inputString = inputBuilderName.withSuffix("/${inputColor.serializedName}").toString()

        OMNI_PRISMATIC_CRUCIBLE_RECIPE.asNotNull().recipeBuilder(builder.id.path)
            .apply {
                input.computeIfAbsent(GTRecipeCapabilities.ITEM) {
                    mutableListOf()
                }.asNotNull().addAll(builder.input[GTRecipeCapabilities.ITEM] ?: emptyList())

                input.computeIfAbsent(GTRecipeCapabilities.FLUID) {
                    mutableListOf()
                }.asNotNull().addAll(builder.input[GTRecipeCapabilities.FLUID] ?: emptyList())

                output.computeIfAbsent(GTRecipeCapabilities.ITEM) {
                    mutableListOf()
                }.asNotNull().addAll(builder.output[GTRecipeCapabilities.ITEM] ?: emptyList())

                output.computeIfAbsent(GTRecipeCapabilities.FLUID) {
                    mutableListOf()
                }.asNotNull().addAll(builder.output[GTRecipeCapabilities.FLUID] ?: emptyList())
            }
            .itemNotConsumableWithName(ItemStack(Items.PAPER), inputString)
            .duration(builder.duration)
            .EUt(builder.EUt().totalEU)
            .save(provider)
    }

    fun getRawItem(content: Any?): Item? {
        return when (content) {
            is ItemStack -> content.item
            is Ingredient -> content.items.firstOrNull()?.item
            else -> null
        }
    }

    @JvmStatic
    fun onMicroverseRecipeBuild(builder: GTRecipeBuilder, provider: Consumer<FinishedRecipe>) {
        val universeType = builder.input[MoniRecipeCapabilities.MICROVERSE]?.firstOrNull()?.content.asType<Microverse>()?.key ?: 0

        val inputCounts = mutableMapOf<Item, Long>()
        val outputCounts = mutableMapOf<Item, Long>()

        builder.input[GTRecipeCapabilities.ITEM]?.forEach { content ->
            (content.content as? Ingredient)?.items?.firstOrNull()?.let { stack ->
                inputCounts[stack.item] = inputCounts.getOrDefault(stack.item, 0L) + stack.count.toLong()
            }
        }

        builder.output[GTRecipeCapabilities.ITEM]?.forEach { content ->
            (content.content as? Ingredient)?.items?.firstOrNull()?.let { stack ->
                outputCounts[stack.item] = outputCounts.getOrDefault(stack.item, 0L) + stack.count.toLong()
            }
        }

        val recipeBuilder = EYE_OF_HARMONY_RECIPE.asNotNull().recipeBuilder(builder.id.path)
            .circuitMeta(universeType)
            .duration(builder.duration)
            .EUt(builder.EUt().totalEU)

        val allItems = inputCounts.keys + outputCounts.keys
        allItems.forEach { item ->
            val inCount = inputCounts.getOrDefault(item, 0L)
            val outCount = outputCounts.getOrDefault(item, 0L)

            when {
                inCount == 0L -> recipeBuilder.outputItems(item, outCount.toInt())
                outCount == 0L -> recipeBuilder.inputItems(item, inCount.toInt())
                inCount == outCount -> recipeBuilder.notConsumable(item)
                inCount > outCount -> {
                    recipeBuilder.notConsumable(item)
                    recipeBuilder.inputItems(item, (inCount - outCount).toInt())
                }
                else -> {
                    recipeBuilder.notConsumable(item)
                    recipeBuilder.outputItems(item, (outCount - inCount).toInt())
                }
            }
        }

        builder.input[GTRecipeCapabilities.FLUID]?.let {
            recipeBuilder.input.computeIfAbsent(GTRecipeCapabilities.FLUID) { ArrayList() }.addAll(it)
        }
        builder.output[GTRecipeCapabilities.FLUID]?.let {
            recipeBuilder.output.computeIfAbsent(GTRecipeCapabilities.FLUID) { ArrayList() }.addAll(it)
        }

        recipeBuilder.save(provider)
    }
}