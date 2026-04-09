package com.yiran.minecraft.gtmqol.api

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility

object RecipeModifierPartMachines {
    private val modifierPartMachines = mutableListOf<IRecipeModifierPartMachine>()

    public val QOL_RECIPE_MODIFIER = PartAbility("qol_recipe_modifier")
}