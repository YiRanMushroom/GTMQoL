package com.yiran.minecraft.gtmqol.logic

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.yiran.minecraft.gtmqol.predicates.JsonTextNBTPredicate
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack


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
        val copied = this.copy()
        copied.parallels = source.parallels
        copied.subtickParallels = source.subtickParallels
        copied.ocLevel = source.ocLevel
        copied.batchParallels = source.batchParallels
        copied.duration = source.duration
        return copied
    }


    interface Optimizer<T> {
        fun optimize(input: List<Content>, output: List<Content>): Pair<List<Content>, List<Content>>

        companion object {
            fun <T> builder(capability: RecipeCapability<*>): Builder<T> = Builder(capability)
        }

        class Builder<T>(val capability: RecipeCapability<*>) {
            private var accessInner: ((Content) -> T?)? = null
            private var accessAmount: ((Content) -> Long)? = null
            private var factory: ((T, Long, Int) -> Content)? = null

            fun accessInner(block: (Content) -> T?) = apply { this.accessInner = block }

            fun accessAmount(block: (Content) -> Long) = apply { this.accessAmount = block }

            fun factory(block: (T, Long, Int) -> Content) = apply { this.factory = block }

            fun build(): Optimizer<T> {
                val inner = requireNotNull(accessInner)
                val amount = requireNotNull(accessAmount)
                val fac = requireNotNull(factory)

                return object : Optimizer<T> {
                    override fun optimize(
                        input: List<Content>,
                        output: List<Content>
                    ): Pair<List<Content>, List<Content>> {
                        val inIds = input.mapNotNull { inner(it) }.toSet()
                        val outIds = output.mapNotNull { inner(it) }.toSet()
                        val volatileIds = inIds.intersect(outIds)

                        val resultIn = mutableListOf<Content>()
                        val resultOut = mutableListOf<Content>()

                        val inCounts = mutableMapOf<T, Long>()
                        input.forEach { content ->
                            val id = inner(content)
                            if (id != null && id in volatileIds) {
                                inCounts[id] = (inCounts[id] ?: 0L) + amount(content)
                            } else {
                                resultIn.add(content.copy(capability))
                            }
                        }

                        val outCounts = mutableMapOf<T, Long>()
                        output.forEach { content ->
                            val id = inner(content)
                            if (id != null && id in volatileIds) {
                                outCounts[id] = (outCounts[id] ?: 0L) + amount(content)
                            } else {
                                resultOut.add(content.copy(capability))
                            }
                        }

                        volatileIds.forEach { id ->
                            val nIn = inCounts[id] ?: 0L
                            val nOut = outCounts[id] ?: 0L

                            when {
                                nIn == nOut -> {
                                    resultIn.add(fac(id, nIn, 0))
                                }

                                nIn > nOut -> {
                                    resultIn.add(fac(id, nIn - nOut, ChanceLogic.getMaxChancedValue()))
                                }

                                else -> {
                                    resultIn.add(fac(id, nIn, 0))
                                    resultOut.add(fac(id, nOut - nIn, ChanceLogic.getMaxChancedValue()))
                                }
                            }
                        }

                        return Pair(resultIn, resultOut)
                    }
                }
            }
        }
    }

    @JvmStatic
    fun getItemIdentity(content: Content): Item? = when (val obj = content.content) {
        is ItemStack -> obj.item
        is Ingredient -> obj.items.firstOrNull()?.item
        else -> null
    }

    @JvmStatic
    fun getItemAmount(content: Content): Long = when (val obj = content.content) {
        is ItemStack -> obj.count.toLong()
        is Ingredient -> obj.items.firstOrNull()?.count?.toLong() ?: 0L
        else -> 0L
    }

    @JvmStatic
    fun createItemContent(id: Item, amount: Long, chance: Int): Content {
        return Content(ItemStack(id, amount.toInt()), chance, ChanceLogic.getMaxChancedValue(), 0)
    }

    @JvmStatic
    fun getFluidIdentity(content: Content): Fluid? = when (val obj = content.content) {
        is FluidStack -> obj.fluid
        is FluidIngredient -> obj.getStacks().firstOrNull()?.fluid
        else -> null
    }

    @JvmStatic
    fun getFluidAmount(content: Content): Long = when (val obj = content.content) {
        is FluidStack -> obj.amount.toLong()
        is FluidIngredient -> obj.getStacks().firstOrNull()?.amount?.toLong() ?: 0L
        else -> 0L
    }

    @JvmStatic
    fun createFluidContent(id: Fluid, amount: Long, chance: Int): Content {
        return Content(FluidStack(id, amount.toInt()), chance, ChanceLogic.getMaxChancedValue(), 0)
    }

    val ITEM_OPTIMIZER: Optimizer<Item> = Optimizer.builder<Item>(GTRecipeCapabilities.ITEM)
        .accessInner(RecipeUtils::getItemIdentity)
        .accessAmount(RecipeUtils::getItemAmount)
        .factory(RecipeUtils::createItemContent)
        .build()

    val FLUID_OPTIMIZER: Optimizer<Fluid> = Optimizer.builder<Fluid>(GTRecipeCapabilities.FLUID)
        .accessInner(RecipeUtils::getFluidIdentity)
        .accessAmount(RecipeUtils::getFluidAmount)
        .factory(RecipeUtils::createFluidContent)
        .build()
}