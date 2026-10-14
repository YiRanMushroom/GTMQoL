package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTEmiRecipe;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.stack.EmiStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * gtceu 1.21 bug: {@code GTEmiRecipe.getOutputs} builds fluid outputs from the fluid and amount only, dropping data
 * components. EMI looks up recipes (R / click) by these outputs, so potion fluids found no recipe, while uses (U) go
 * through the inputs and worked. Keep the components. Remove once gtceu fixes it.
 */
@Mixin(value = GTEmiRecipe.class, remap = false)
public class GTEmiRecipeMixin {

    @WrapOperation(method = "getOutputs",
                   at = @At(value = "INVOKE",
                            target = "Ldev/emi/emi/api/stack/EmiStack;of(Lnet/minecraft/world/level/material/Fluid;J)Ldev/emi/emi/api/stack/EmiStack;"))
    private EmiStack gtmqol$keepComponents(Fluid fluid, long amount, Operation<EmiStack> original,
                                           @Local FluidStack stack) {
        return EmiStack.of(fluid, stack.getComponentsPatch(), amount);
    }
}
