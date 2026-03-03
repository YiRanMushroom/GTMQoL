package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.MULTIBLOCK
import com.gregtechceu.gtceu.common.data.GTSoundEntries
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture
import com.yiran.minecraft.gtmqol.ae2PresentedAndIntegrationEnabled
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import com.yiran.minecraft.gtmqol.logic.GreenhouseCustomRecipeLogic
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

object QoLRecipeTypes {
    @JvmField
    var GREENHOUSE_RECIPES: GTRecipeType? = null

    @JvmField
    var ME_ASSEMBLER_RECIPES: GTRecipeType? = null

    @JvmField
    var ME_CIRCUIT_SLICER_RECIPES: GTRecipeType? = null

    @JvmField
    var MAGICAL_ASSEMBLER_RECIPES: GTRecipeType? = null

    @JvmField
    var ELECTRIC_IMPLOSION_RECIPES: GTRecipeType? = null

    init {
        if (ConfigHolder.instance.addonConfig.enableGreenhouse) {
            GREENHOUSE_RECIPES = GTRecipeTypes.register("gtmqol:greenhouse", "electric")
                .setMaxIOSize(6, 6, 3, 3)
                .setEUIO(IO.IN)
                .addCustomRecipeLogic(GreenhouseCustomRecipeLogic())
                .prepareBuilder { recipeBuilder ->
                    recipeBuilder.EUt(GTValues.VA[1].toLong())
                }
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.REPLICATOR)
        }

        if (ae2PresentedAndIntegrationEnabled()) {
            ME_ASSEMBLER_RECIPES = GTRecipeTypes.register("gtmqol:me_assembler", "electric")
                .setMaxIOSize(6, 1, 3, 0)
                .setEUIO(IO.IN)
                .prepareBuilder { recipeBuilder ->
                    recipeBuilder.EUt(GTValues.VA[1].toLong())
                }
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.ASSEMBLER)

            ME_CIRCUIT_SLICER_RECIPES = GTRecipeTypes.register("gtmqol:me_circuit_slicer", "electric")
                .setMaxIOSize(1, 1, 0, 0)
                .setEUIO(IO.IN)
                .prepareBuilder { recipeBuilder ->
                    recipeBuilder.EUt(GTValues.VA[1].toLong())
                }
                .setProgressBar(GuiTextures.PROGRESS_BAR_SLICE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.CUT)

        }

        MAGICAL_ASSEMBLER_RECIPES = GTRecipeTypes.register("gtmqol:magical_assembler", "electric")
            .setMaxIOSize(6, 3, 3, 3)
            .setEUIO(IO.IN)
            .prepareBuilder { recipeBuilder ->
                recipeBuilder.EUt(GTValues.VA[1].toLong())
            }
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE)

        if (ConfigHolder.instance.addonConfig.enableElectricImplosionRecipes) {
            ELECTRIC_IMPLOSION_RECIPES = GTRecipeTypes.register("gtmqol:electric_implosion_compressor", MULTIBLOCK)
                .setMaxIOSize(3, 2, 0, 0).setEUIO(IO.IN)
                .prepareBuilder({ recipeBuilder -> recipeBuilder.duration(20).EUt(GTValues.VA[GTValues.LV].toLong()) })
                .setSlotOverlay(false, false, true, GuiTextures.IMPLOSION_OVERLAY_1)
                .setSlotOverlay(false, false, false, GuiTextures.IMPLOSION_OVERLAY_2)
                .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                .setSound(ExistingSoundEntry(SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS))
        }
    }

    @JvmStatic
    fun init() {
        // This function is intentionally left blank. It serves as an initializer for the QoLRecipeTypes object.
    }
}