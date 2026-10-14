package com.yiran.minecraft.gtmqol.functionality

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import net.minecraft.data.recipes.FinishedRecipe
import java.util.function.BiConsumer
import java.util.function.Consumer

interface IAppendOnBuildAction {
    fun appendOnRecipeBuild(onSave: BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>>)
}