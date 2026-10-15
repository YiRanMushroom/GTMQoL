package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.CannerLogic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;

/**
 * gtceu's canner logic fills and drains any fluid container at runtime but has no representative recipes, so the
 * recipe viewer shows none. Add bottling and draining for every potion, like {@code BreweryLogic} does for brewing.
 */
@Mixin(value = CannerLogic.class, remap = false)
public class CannerLogicMixin {

    public void buildRepresentativeRecipes() {
        for (Potion potion : BuiltInRegistries.POTION) {
            // the empty potion has no fluid
            if (potion == Potions.EMPTY) continue;
            ResourceLocation id = BuiltInRegistries.POTION.getKey(potion);
            String name = id.getNamespace() + "_" + id.getPath();
            ItemStack bottle = PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
            FluidStack fluid = PotionFluidHelper.getFluidFromPotion(potion, PotionFluidHelper.BOTTLE_AMOUNT);

            // a glass bottle only takes potion fluids, so water can't be bottled here
            if (potion != Potions.WATER) {
                gtmqol$add(GTRecipeTypes.CANNER_RECIPES.recipeBuilder("fill_potion_" + name)
                        .inputItems(new ItemStack(Items.GLASS_BOTTLE))
                        .inputFluids(fluid)
                        .outputItems(bottle)
                        .duration(16)
                        .EUt(4)
                        .buildRawRecipe());
            }
            gtmqol$add(GTRecipeTypes.CANNER_RECIPES.recipeBuilder("drain_potion_" + name)
                    .inputItems(bottle)
                    .outputItems(new ItemStack(Items.GLASS_BOTTLE))
                    .outputFluids(fluid)
                    .duration(16)
                    .EUt(4)
                    .buildRawRecipe());
        }
    }

    private static void gtmqol$add(GTRecipe recipe) {
        // for the recipe viewer to detect it's a synthetic recipe (not ever in JSON)
        recipe.setId(recipe.getId().withPrefix("/"));
        GTRecipeTypes.CANNER_RECIPES.addToMainCategory(recipe);
    }
}
