package com.yiran.minecraft.gtmqol.api

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

abstract class AbstractRecipeModifierPartMachine(holder: IMachineBlockEntity) : MultiblockPartMachine(holder),
    IRecipeModifierPartMachine {
    abstract override fun getRecipeModifier(): ModifierFunction

    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    override fun modifyRecipe(recipe: GTRecipe): GTRecipe? {
        return getRecipeModifier().apply(recipe)
    }

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(
            AbstractRecipeModifierPartMachine::class.java, MultiblockPartMachine.MANAGED_FIELD_HOLDER)
    }
}