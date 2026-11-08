package com.yiran.minecraft.gtmqol.common.potion;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.gregtechceu.gtceu.api.GTValues.VHA;

/**
 * Brews splash and lingering potion fluids (see {@link PotionContainers}): vanilla's container mixes, which gtceu's
 * brewery skips since they turn one potion item into another. Added after gtceu's own brewery logic, so it is asked
 * only when that finds nothing.
 */
// Untested: the brewery still brews splash and lingering potion fluids, and EMI still shows those recipes and the
// canner's potion filling and draining recipes
public enum PotionMixBrewingLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        List<PotionBrewing.Mix<Item>> mixes = PotionContainers.mixes();
        if (mixes.isEmpty()) return null;
        List<ItemStack> notDistinctItems = new ArrayList<>();
        List<FluidStack> notDistinctFluids = new ArrayList<>();
        for (RecipeHandlerList handlerList : holder.getCapabilitiesForIO(IO.IN)) {
            boolean distinct = handlerList.isDistinct();
            List<ItemStack> items = distinct ? new ArrayList<>() : notDistinctItems;
            List<FluidStack> fluids = distinct ? new ArrayList<>() : notDistinctFluids;
            for (IRecipeHandler<?> handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (content instanceof ItemStack stack && !stack.isEmpty()) items.add(stack);
                }
            }
            for (IRecipeHandler<?> handler : handlerList.getCapability(FluidRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (content instanceof FluidStack stack && !stack.isEmpty()) fluids.add(stack);
                }
            }
            if (distinct) {
                GTRecipe recipe = containerMix(mixes, items, fluids);
                if (recipe != null) return recipe;
            }
        }
        return containerMix(mixes, notDistinctItems, notDistinctFluids);
    }

    private static @Nullable GTRecipe containerMix(List<PotionBrewing.Mix<Item>> mixes, List<ItemStack> items,
                                                          List<FluidStack> fluids) {
        for (FluidStack fluid : fluids) {
            if (!PotionContainers.isPotion(fluid)) continue;
            Item container = PotionContainers.container(fluid);
            for (PotionBrewing.Mix<Item> mix : mixes) {
                if (mix.from().value() != container) continue;
                for (ItemStack item : items) {
                    if (mix.ingredient().test(item)) return recipe("potion_container_mix", fluid, mix);
                }
            }
        }
        return null;
    }

    private static GTRecipe recipe(String id, FluidStack potion, PotionBrewing.Mix<Item> mix) {
        FluidStack from = potion.copyWithAmount(PotionFluidHelper.MB_PER_RECIPE);
        // the brewery has no item output, so dragon's breath doesn't leave its bottle
        return GTRecipeTypes.BREWING_RECIPES.recipeBuilder(id)
                .inputItems(mix.ingredient())
                .inputFluids(from)
                .outputFluids(PotionContainers.withContainer(from, mix.to().value()))
                .duration(400)
                .EUt(VHA[MV])
                .build();
    }

    @Override
    public void buildRepresentativeRecipes() {
        List<PotionBrewing.Mix<Item>> mixes = PotionContainers.mixes();
        BuiltInRegistries.POTION.holders().forEach(potion -> {
            // water is plain water, not a potion fluid
            if (potion == Potions.WATER) return;
            ResourceLocation id = potion.key().location();
            FluidStack fluid = PotionFluidHelper.getFluidFromPotion(potion, PotionFluidHelper.MB_PER_RECIPE);
            for (PotionBrewing.Mix<Item> mix : mixes) {
                String to = BuiltInRegistries.ITEM.getKey(mix.to().value()).getPath();
                GTRecipe recipe = recipe(to + "_" + id.getNamespace() + "_" + id.getPath(),
                        PotionContainers.withContainer(fluid, mix.from().value()), mix);
                // for EMI to detect it's a synthetic recipe (not ever in JSON)
                recipe.setId(recipe.getId().withPrefix("/"));
                GTRecipeTypes.BREWING_RECIPES.addToMainCategory(recipe);
            }
        });
    }
}
