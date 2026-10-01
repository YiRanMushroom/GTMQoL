package com.yiran.minecraft.gtmqol.mixin.modpack_integration.monifactory;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.neganote.monilabs.common.machine.multiblock.MicroverseProjectorMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(
        require = @Condition(type = Condition.Type.MOD, value = "monilabs"))
@Mixin(MicroverseProjectorMachine.class)
public class MicroverseProjectionMachineMixin {
    @Definition(id = "decayRate", local = @Local(type = int.class, name = "decayRate"))
    @Expression("decayRate = decayRate * @(?)")
    @ModifyExpressionValue(method = "onWorking", at = @At("MIXINEXTRAS:EXPRESSION"))
    public int modifyDecayMultiplier(int decayRate) {
        if (ConfigHolder.getInstance().addonConfig.enableMoniFactoryIntegration) {
            return 1;
        } else {
            return decayRate;
        }
    }
}
