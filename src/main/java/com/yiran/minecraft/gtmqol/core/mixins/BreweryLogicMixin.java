package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.BreweryLogic;
import com.yiran.minecraft.gtmqol.common.potion.PotionContainers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.gregtechceu.gtceu.api.GTValues.VHA;

/**
 * Brews splash and lingering potion fluids (see {@link PotionContainers}): vanilla's container mixes, which gtceu's
 * brewery skips since they turn one potion item into another.
 */
@Mixin(value = BreweryLogic.class, remap = false)
public class BreweryLogicMixin {

    @Inject(method = "createCustomRecipe", at = @At("RETURN"), cancellable = true)
    private void gtmqol$containerMix(IRecipeCapabilityHolder holder, CallbackInfoReturnable<GTRecipe> cir) {
        if (cir.getReturnValue() != null) return;
        List<PotionBrewing.Mix<Item>> mixes = PotionContainers.mixes();
        if (mixes.isEmpty()) return;
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
                GTRecipe recipe = gtmqol$containerMix(mixes, items, fluids);
                if (recipe != null) {
                    cir.setReturnValue(recipe);
                    return;
                }
            }
        }
        GTRecipe recipe = gtmqol$containerMix(mixes, notDistinctItems, notDistinctFluids);
        if (recipe != null) cir.setReturnValue(recipe);
    }

    private static @Nullable GTRecipe gtmqol$containerMix(List<PotionBrewing.Mix<Item>> mixes, List<ItemStack> items,
                                                          List<FluidStack> fluids) {
        for (FluidStack fluid : fluids) {
            if (!PotionContainers.isPotion(fluid)) continue;
            Item container = PotionContainers.container(fluid);
            for (PotionBrewing.Mix<Item> mix : mixes) {
                if (mix.from.get() != container) continue;
                for (ItemStack item : items) {
                    if (mix.ingredient.test(item)) return gtmqol$recipe("potion_container_mix", fluid, mix);
                }
            }
        }
        return null;
    }

    private static GTRecipe gtmqol$recipe(String id, FluidStack potion, PotionBrewing.Mix<Item> mix) {
        FluidStack from = new FluidStack(potion, PotionFluidHelper.MB_PER_RECIPE);
        // the brewery has no item output, so dragon's breath doesn't leave its bottle
        return GTRecipeTypes.BREWING_RECIPES.recipeBuilder(id)
                .inputItems(mix.ingredient)
                .inputFluids(from)
                .outputFluids(PotionContainers.withContainer(from, mix.to.get()))
                .duration(400)
                .EUt(VHA[MV])
                .buildRawRecipe();
    }

    @Inject(method = "buildRepresentativeRecipes", at = @At("TAIL"))
    private void gtmqol$containerMixRecipes(CallbackInfo ci) {
        List<PotionBrewing.Mix<Item>> mixes = PotionContainers.mixes();
        for (Potion potion : BuiltInRegistries.POTION) {
            // water is plain water, not a potion fluid; the empty potion has no fluid
            if (potion == Potions.WATER || potion == Potions.EMPTY) continue;
            ResourceLocation id = BuiltInRegistries.POTION.getKey(potion);
            FluidStack fluid = PotionFluidHelper.getFluidFromPotion(potion, PotionFluidHelper.MB_PER_RECIPE);
            for (PotionBrewing.Mix<Item> mix : mixes) {
                String to = BuiltInRegistries.ITEM.getKey(mix.to.get()).getPath();
                GTRecipe recipe = gtmqol$recipe(to + "_" + id.getNamespace() + "_" + id.getPath(),
                        PotionContainers.withContainer(fluid, mix.from.get()), mix);
                // for the recipe viewer to detect it's a synthetic recipe (not ever in JSON)
                recipe.setId(recipe.getId().withPrefix("/"));
                GTRecipeTypes.BREWING_RECIPES.addToMainCategory(recipe);
            }
        }
    }
}
