package com.yiran.minecraft.gtmqol.common.multiblocks.parts

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.yiran.minecraft.gtmqol.api.AbstractRecipeModifierPartMachine

class ProbableImprobabilityDevice(holder: IMachineBlockEntity) : AbstractRecipeModifierPartMachine(holder) {
    override fun getRecipeModifier(): ModifierFunction {
        return ModifierFunction { recipe ->
            recipe.copy().apply {
                inputs.entries.forEach { (key, value) ->
                    this.inputs[key] = value.map(ProbableImprobabilityDevice::modifierInputContent)
                }
            }
        }
    }

    companion object {
        fun modifierInputContent(value: Content): Content {
            return if (value.chance != ChanceLogic.getMaxChancedValue()) {
                Content(value.content, 0, ChanceLogic.getMaxChancedValue(), 0)
            } else {
                value
            }
        }
    }
}