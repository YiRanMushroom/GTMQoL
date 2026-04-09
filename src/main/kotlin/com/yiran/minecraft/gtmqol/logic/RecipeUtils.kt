package com.yiran.minecraft.gtmqol.logic

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.RecipeHelper
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.yiran.minecraft.gtmqol.predicates.JsonTextNBTPredicate
import net.minecraft.advancements.AdvancementRewards.Builder.recipe
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack


object RecipeUtils {
    fun GTRecipeBuilder.itemInputWithName(itemStack: ItemStack, name: String): GTRecipeBuilder {
        val newStack = itemStack.copy()

        newStack.setHoverName(Component.literal(name))

        val predicate = JsonTextNBTPredicate(
            "${ItemStack.TAG_DISPLAY}.${ItemStack.TAG_DISPLAY_NAME}",
            name
        )

        return this.inputItemNbtPredicate(newStack, predicate)
    }

    fun GTRecipeBuilder.itemNotConsumableWithName(itemStack: ItemStack, name: String): GTRecipeBuilder {
        val lastChance = this.chance
        this.chance = 0
        this.itemInputWithName(itemStack, name)
        this.chance = lastChance
        return this
    }

    fun GTRecipe.copyMutableFrom(source: GTRecipe): GTRecipe {
        parallels = source.parallels
        subtickParallels = source.subtickParallels
        ocLevel = source.ocLevel
        batchParallels = source.batchParallels
        duration = source.duration
        return this
    }
}