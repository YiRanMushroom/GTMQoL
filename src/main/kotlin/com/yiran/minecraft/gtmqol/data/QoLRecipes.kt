package com.yiran.minecraft.gtmqol.data

import com.yiran.minecraft.gtmqol.GTMQoL
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import java.util.function.Consumer

object QoLRecipes {
    fun init(provider: Consumer<FinishedRecipe>) {
        QoLRecipeTypes.GREEN_HOUSE_RECIPES.recipeBuilder(GTMQoL.id("oak_testing"))
            .notConsumable(ItemStack(Items.OAK_SAPLING))
            .inputFluids(FluidStack(Fluids.WATER, 100))
            .circuitMeta(1)
            .chancedOutput(ItemStack(Items.OAK_SAPLING, 2), 2000, 1500)
            .outputItems(ItemStack(Items.OAK_LOG, 8))
            .chancedOutput(ItemStack(Items.APPLE, 1), 1000, 500)
            .chancedOutput(ItemStack(Items.STICK, 4), 4000, 2000)
            .save(provider)
    }
}