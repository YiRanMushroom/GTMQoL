package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.GTSoundEntries
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture

object QoLRecipeTypes {
    val GREEN_HOUSE_RECIPES: GTRecipeType

    init {
        GREEN_HOUSE_RECIPES = GTRecipeTypes.register("gtmqol:greenhouse", "electric")
            .setMaxIOSize(6, 6, 3, 3)
            .setEUIO(IO.IN)
            .prepareBuilder { recipeBuilder ->
                recipeBuilder.EUt(GTValues.VA[1].toLong())
            }/*.setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
            .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
            .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)*/
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.REPLICATOR)
    }

    @JvmStatic
    fun init() {
        // This function is intentionally left blank. It serves as an initializer for the QoLRecipeTypes object.
    }
}