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
import com.yiran.minecraft.gtmqol.logic.RecipeUtils.itemNotConsumableWithName
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.neganote.monilabs.capability.recipe.ChromaIngredient
import net.neganote.monilabs.capability.recipe.MoniRecipeCapabilities
import net.neganote.monilabs.common.machine.multiblock.Color
import java.util.function.Consumer


object MoniRecipeTypesExtension {
    @JvmStatic
    var OMNI_PRISMATIC_CRUCIBLE_RECIPE: GTRecipeType? = null

    @JvmStatic
    var EYE_OF_HARMONY_RECIPE: GTRecipeType? = null

    init {
        if (MoniUtils.isMoniFactoryAndIntegrationEnabled()) {
            OMNI_PRISMATIC_CRUCIBLE_RECIPE =
                GTRecipeTypes
                    .register("omni_prismatic_recipe", GTRecipeTypes.MULTIBLOCK)
                    .setMaxIOSize(6, 6, 3, 3)
                    .setEUIO(IO.IN)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                    .setSound(GTSoundEntries.ELECTROLYZER)
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
}