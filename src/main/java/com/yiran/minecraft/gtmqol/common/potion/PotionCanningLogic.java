package com.yiran.minecraft.gtmqol.common.potion;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * gtceu's canner logic fills and drains any fluid container at runtime but has no representative recipes, so EMI shows
 * none. Add bottling and draining for every potion, like {@code BreweryLogic} does for brewing, also as splash and
 * lingering potions (see {@link PotionContainers}).
 */
public enum PotionCanningLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    /** Nothing to run: gtceu's canner logic already fills and drains potions. */
    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        return null;
    }

    @Override
    public void buildRepresentativeRecipes() {
        Set<Item> containers = new LinkedHashSet<>();
        containers.add(Items.POTION);
        for (PotionBrewing.Mix<Item> mix : PotionContainers.mixes()) containers.add(mix.to().value());
        BuiltInRegistries.POTION.holders().forEach(potion -> {
            ResourceLocation id = potion.key().location();
            FluidStack potionFluid = PotionFluidHelper.getFluidFromPotion(potion, PotionFluidHelper.BOTTLE_AMOUNT);
            for (Item container : containers) {
                String name = id.getNamespace() + "_" + id.getPath();
                if (container != Items.POTION) {
                    // splash / lingering water drains to plain water, there's no marked water
                    if (potion == Potions.WATER) continue;
                    name = BuiltInRegistries.ITEM.getKey(container).getPath() + "_" + name;
                }
                ItemStack bottle = PotionContents.createItemStack(container, potion);
                FluidStack fluid = PotionContainers.withContainer(potionFluid, container);

                // a glass bottle only takes potion fluids, so water can't be bottled here
                if (potion != Potions.WATER) {
                    add(GTRecipeTypes.CANNER_RECIPES.recipeBuilder("fill_potion_" + name)
                            .inputItems(new ItemStack(Items.GLASS_BOTTLE))
                            .inputFluids(fluid)
                            .outputItems(bottle)
                            .duration(16)
                            .EUt(4)
                            .build());
                }
                add(GTRecipeTypes.CANNER_RECIPES.recipeBuilder("drain_potion_" + name)
                        .inputItems(bottle)
                        .outputItems(new ItemStack(Items.GLASS_BOTTLE))
                        .outputFluids(fluid)
                        .duration(16)
                        .EUt(4)
                        .build());
            }
        });
    }

    private static void add(GTRecipe recipe) {
        // for EMI to detect it's a synthetic recipe (not ever in JSON)
        recipe.setId(recipe.getId().withPrefix("/"));
        GTRecipeTypes.CANNER_RECIPES.addToMainCategory(recipe);
    }
}
