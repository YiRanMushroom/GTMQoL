package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

/**
 * Fusion uses perfect sub-tick overclocking (which {@code OverclockingLogicMixin} turns into our 2x power / 4x
 * speed) with the full hatch voltage instead of being capped at the reactor's tier. The structure side (substation
 * and laser hatches) is in {@code GTMultiMachinesMixin}.
 */
@Mixin(value = FusionReactorMachine.class, remap = false)
public class FusionReactorMachineMixin {

    @WrapOperation(method = "<clinit>",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;create(DDZ)Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;"))
    private static OverclockingLogic gtmqol$fusionOC(double durationFactor, double voltageFactor, boolean subTick,
                                                     Operation<OverclockingLogic> original) {
        if (!gtmqol$enabled()) return original.call(durationFactor, voltageFactor, subTick);
        return OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK;
    }

    @WrapOperation(method = "recipeModifier",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;getModifier(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;JZ)Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;"))
    private static ModifierFunction gtmqol$fusionOCVoltage(OverclockingLogic logic, MetaMachine machine,
                                                           GTRecipe recipe, long maxVoltage, boolean shouldParallel,
                                                           Operation<ModifierFunction> original) {
        if (!gtmqol$enabled()) return original.call(logic, machine, recipe, maxVoltage, shouldParallel);
        return original.call(logic, machine, recipe, ((FusionReactorMachine) machine).getOverclockVoltage(), true);
    }

    private static boolean gtmqol$enabled() {
        return GTMQoLConfig.INSTANCE != null && GTMQoLConfig.INSTANCE.overclocking.buffFusionReactor;
    }
}
