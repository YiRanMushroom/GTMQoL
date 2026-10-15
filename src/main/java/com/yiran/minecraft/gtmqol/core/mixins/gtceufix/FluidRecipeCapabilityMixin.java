package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IRangedIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

/**
 * gtceu bug: {@code inputFluids(FluidStack)} with NBT (potions in the brewery and canner) makes an ingredient over the
 * fluid's tag, which the recipe viewer gets as a tag list. EMI merges the tag's stacks (potion and flowing potion) back
 * into the tag by fluid alone, dropping the NBT, so every potion slot showed Uncraftable Potion and U on a potion found
 * all of them. List a single source fluid instead, so there's nothing to merge. Remove once gtceu fixes it.
 */
@Mixin(value = FluidRecipeCapability.class, remap = false)
public class FluidRecipeCapabilityMixin {

    @Inject(method = "mapIngredientToEntryList", at = @At("HEAD"), cancellable = true)
    private static void gtmqol$keepNbt(FluidIngredient ingredient, CallbackInfoReturnable<FluidEntryList> cir) {
        CompoundTag nbt = ingredient.getNbt();
        if (nbt == null || nbt.isEmpty() || ingredient instanceof IRangedIngredient) return;
        List<FluidStack> sources = Arrays.stream(ingredient.getStacks())
                .filter(stack -> stack.getFluid().isSource(stack.getFluid().defaultFluidState()))
                .toList();
        if (sources.isEmpty()) return;
        // other mods' fluids in the tag (IE and Create potions) would be merged the same way, so show only one,
        // gtceu's if there is one, which is also what a pattern transfer then takes
        FluidStack shown = sources.stream()
                .filter(stack -> BuiltInRegistries.FLUID.getKey(stack.getFluid()).getNamespace().equals(GTCEu.MOD_ID))
                .findFirst()
                .orElse(sources.get(0));
        // getStacks is cached by the ingredient
        cir.setReturnValue(FluidStackList.of(shown.copy()));
    }
}
