package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

/**
 * gtceu 1.21 bug: {@code inputFluids(FluidStack)} with components (potions in the brewery and canner) makes a component
 * ingredient over the fluid's tag, which the recipe viewer gets as a tag list. EMI merges the tag's stacks (potion and
 * flowing potion) back into the tag by fluid alone, dropping the components, so every potion slot showed Uncraftable
 * Potion and U on a potion found all of them. List a single source fluid instead, so there's nothing to merge.
 * Remove once gtceu fixes it.
 */
@Mixin(value = FluidRecipeCapability.class, remap = false)
public class FluidRecipeCapabilityMixin {

    @Inject(method = "mapIngredientToEntryList", at = @At("HEAD"), cancellable = true)
    private static void gtmqol$keepComponents(SizedFluidIngredient ingredient,
                                              CallbackInfoReturnable<FluidEntryList> cir) {
        if (!(ingredient.ingredient() instanceof DataComponentFluidIngredient component) ||
                component.components().alwaysMatches()) {
            return;
        }
        List<FluidStack> sources = Arrays.stream(ingredient.getFluids())
                .filter(stack -> stack.getFluid().isSource(stack.getFluid().defaultFluidState()))
                .toList();
        if (sources.isEmpty()) return;
        // other mods' fluids in the tag (IE and Create potions in ATM10) would be merged the same way, so show only
        // one, gtceu's if there is one, which is also what a pattern transfer then takes
        FluidStack shown = sources.stream()
                .filter(stack -> BuiltInRegistries.FLUID.getKey(stack.getFluid()).getNamespace().equals(GTCEu.MOD_ID))
                .findFirst()
                .orElse(sources.getFirst());
        cir.setReturnValue(FluidStackList.of(shown));
    }
}
