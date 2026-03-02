package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FusionReactorMachine.class)
public class FusionReactor$ChangeOverclockingLogic$Mixin {
    @Definition(id = "FUSION_OC", field = "Lcom/gregtechceu/gtceu/common/machine/multiblock/electric/FusionReactorMachine;FUSION_OC:Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;")
    @Definition(id = "getModifier", method = "Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;getModifier(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;JZ)Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;")
    @Expression("FUSION_OC.getModifier(?, ?, ?, ?)")
    @WrapOperation(method = "recipeModifier", at = @At(value = "MIXINEXTRAS:EXPRESSION"), remap = false)
    private static ModifierFunction fusionUseOverclockingLogic(OverclockingLogic instance, MetaMachine metaMachine, GTRecipe gtRecipe, long voltage, boolean parallel, Operation<ModifierFunction> original) {
        if (ConfigHolder.getInstance().overclockingConfig.buffFusionReactorOverclocking) {
            return original.call(
                    instance, metaMachine, gtRecipe,
                    ((FusionReactorMachine) metaMachine).getOverclockVoltage(), true);
        }

        return original.call(instance, metaMachine, gtRecipe, voltage, parallel);
    }

    @Definition(id = "FUSION_OC", field = "Lcom/gregtechceu/gtceu/common/machine/multiblock/electric/FusionReactorMachine;FUSION_OC:Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;")
    @Expression("FUSION_OC = @(?)")
    @WrapOperation(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static OverclockingLogic modifyFusionOverclockingLogic(double durationFactor, double voltageFactor, boolean subtick, Operation<OverclockingLogic> original) {
        if (ConfigHolder.getInstance().overclockingConfig.buffFusionReactorOverclocking) {
            return OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK;
        } else {
            return original.call(durationFactor, voltageFactor, subtick);
        }
    }
}
