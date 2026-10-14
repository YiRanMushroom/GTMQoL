package com.yiran.minecraft.gtmqol.integration.monifactory

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.GTSoundEntries
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture
import com.yiran.minecraft.gtmqol.GTMQoL
import com.yiran.minecraft.gtmqol.ModUtils.asNotNull
import com.yiran.minecraft.gtmqol.ModUtils.asType
import com.yiran.minecraft.gtmqol.logic.RecipeUtils
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
import kotlin.collections.mutableListOf


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

//    fun getRawItem(content: Any?): Item? {
//        return when (content) {
//            is ItemStack -> content.item
//            is Ingredient -> content.items.firstOrNull()?.item
//            else -> null
//        }
//    }

    @JvmStatic
    fun onMicroverseRecipeBuild(builder: GTRecipeBuilder, provider: Consumer<FinishedRecipe>) {
        val universe =
            builder.input[MoniRecipeCapabilities.MICROVERSE]?.firstOrNull()?.content?.asType<Microverse>()?.key ?: 0

        val eohBuilder = GTRecipeBuilder(builder.id, EYE_OF_HARMONY_RECIPE.asNotNull())
            .circuitMeta(universe)
            .duration(builder.duration)
            .EUt(builder.EUt().voltage, builder.EUt().amperage)

        val rawRecipe = builder.buildRawRecipe()

        RecipeUtils.ITEM_OPTIMIZER.let { optimizer ->
            val (newIn, newOut) = optimizer.optimize(
                rawRecipe.inputs.getOrDefault(GTRecipeCapabilities.ITEM, emptyList()),
                rawRecipe.outputs.getOrDefault(GTRecipeCapabilities.ITEM, emptyList())
            )
            if (newIn.isNotEmpty()) eohBuilder.input.computeIfAbsent(GTRecipeCapabilities.ITEM) { mutableListOf() }.addAll(newIn)
            if (newOut.isNotEmpty()) eohBuilder.output.computeIfAbsent(GTRecipeCapabilities.ITEM) { mutableListOf() }.addAll(newOut)
        }

        RecipeUtils.FLUID_OPTIMIZER.let { optimizer ->
            val (newIn, newOut) = optimizer.optimize(
                rawRecipe.inputs.getOrDefault(GTRecipeCapabilities.FLUID, emptyList()),
                rawRecipe.outputs.getOrDefault(GTRecipeCapabilities.FLUID, emptyList())
            )
            if (newIn.isNotEmpty()) eohBuilder.input.computeIfAbsent(GTRecipeCapabilities.FLUID) { mutableListOf() }.addAll(newIn)
            if (newOut.isNotEmpty()) eohBuilder.output.computeIfAbsent(GTRecipeCapabilities.FLUID) { mutableListOf() }.addAll(newOut)
        }

        eohBuilder.save(provider)
    }
}