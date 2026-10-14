package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

/**
 * gtceu 1.21 bug: {@code outputFluids(FluidStack)} wraps the stack with {@code SizedFluidIngredient.of}, which keeps
 * only the fluid, so an output's data components are dropped. The brewery outputs a potion fluid without potion
 * contents, and EMI can't find the recipe from a specific potion. Use a strict component ingredient for such stacks.
 * Remove once gtceu fixes it.
 */
@Mixin(value = GTRecipeBuilder.class, remap = false)
public class GTRecipeBuilderMixin {

    @WrapOperation(method = "outputFluids(Lnet/neoforged/neoforge/fluids/FluidStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
                   at = @At(value = "INVOKE",
                            target = "Lnet/neoforged/neoforge/fluids/crafting/SizedFluidIngredient;of(Lnet/neoforged/neoforge/fluids/FluidStack;)Lnet/neoforged/neoforge/fluids/crafting/SizedFluidIngredient;"))
    private SizedFluidIngredient gtmqol$keepComponents(FluidStack stack, Operation<SizedFluidIngredient> original) {
        return stack.isComponentsPatchEmpty() ? original.call(stack) : gtmqol$sized(stack);
    }

    // The varargs overload maps with a method reference, which can't be wrapped, so build the array here instead.
    @Inject(method = "outputFluids([Lnet/neoforged/neoforge/fluids/FluidStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
            at = @At("HEAD"), cancellable = true)
    private void gtmqol$keepComponents(FluidStack[] outputs, CallbackInfoReturnable<GTRecipeBuilder> cir) {
        if (Arrays.stream(outputs).allMatch(FluidStack::isComponentsPatchEmpty)) return;
        SizedFluidIngredient[] ingredients = Arrays.stream(outputs)
                .map(s -> s.isComponentsPatchEmpty() ? SizedFluidIngredient.of(s) : gtmqol$sized(s))
                .toArray(SizedFluidIngredient[]::new);
        cir.setReturnValue(((GTRecipeBuilder) (Object) this).output(FluidRecipeCapability.CAP, ingredients));
    }

    private static SizedFluidIngredient gtmqol$sized(FluidStack stack) {
        return new SizedFluidIngredient(DataComponentFluidIngredient.of(true, stack), stack.getAmount());
    }
}
