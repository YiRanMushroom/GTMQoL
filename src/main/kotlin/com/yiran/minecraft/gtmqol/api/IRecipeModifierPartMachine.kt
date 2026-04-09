package com.yiran.minecraft.gtmqol.api

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier

interface IRecipeModifierPartMachine : IMultiPart {
    fun getRecipeModifier(): ModifierFunction
}