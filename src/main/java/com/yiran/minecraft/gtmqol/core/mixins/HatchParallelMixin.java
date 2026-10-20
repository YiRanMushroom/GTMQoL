package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.yiran.minecraft.gtmqol.common.modifier.AbsoluteParallel;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * overclocking.absoluteParallelHatches: a parallel hatch's parallels cost no extra EU/t ({@link AbsoluteParallel}),
 * so a multiblock runs as many as the hatch allows at the EU/t of one recipe. Generators keep gtceu's behaviour.
 */
@Mixin(value = GTRecipeModifiers.class, remap = false)
public class HatchParallelMixin {

    @Inject(method = "hatchParallel", at = @At("HEAD"), cancellable = true)
    private static void gtmqol$absolute(MetaMachine machine, GTRecipe recipe,
                                        CallbackInfoReturnable<ModifierFunction> cir) {
        if (!GTMQoLConfig.get().overclocking.absoluteParallelHatches) return;
        if (!(machine instanceof MultiblockControllerMachine controller) || !controller.isFormed()) return;
        if (controller.getDefinition().isGenerator()) return;
        controller.getParallelHatch().ifPresent(hatch -> cir.setReturnValue(
                AbsoluteParallel.upTo(hatch.getCurrentParallel()).getModifier(machine, recipe)));
    }
}
