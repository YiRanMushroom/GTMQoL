package com.yiran.minecraft.gtmqol.logic

import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.EqualsNBTPredicate
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.yiran.minecraft.gtmqol.ModUtils.asType
import com.yiran.minecraft.gtmqol.predicates.JsonTextNBTPredicate
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.StringTag
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
}