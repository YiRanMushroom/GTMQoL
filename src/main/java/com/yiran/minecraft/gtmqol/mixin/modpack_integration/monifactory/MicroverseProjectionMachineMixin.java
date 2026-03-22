package com.yiran.minecraft.gtmqol.mixin.modpack_integration.monifactory;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import net.neganote.monilabs.common.machine.multiblock.MicroverseProjectorMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Condition(type = Condition.Type.MOD, value = "monilabs")
@Mixin(MicroverseProjectorMachine.class)
public class MicroverseProjectionMachineMixin {
    @Definition(id = "decayRate", local = @Local(type = int.class, name = "decayRate"))
    @Expression("decayRate = decayRate * @(?)")
    @ModifyExpressionValue(method = "onWorking", at = @At("MIXINEXTRAS:EXPRESSION"))
    public int modifyDecayMultiplier(int decayRate) {
        return 1;
    }
}
