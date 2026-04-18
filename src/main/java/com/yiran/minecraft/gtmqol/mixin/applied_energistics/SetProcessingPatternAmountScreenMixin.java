package com.yiran.minecraft.gtmqol.mixin.applied_energistics;

import appeng.client.gui.me.items.SetProcessingPatternAmountScreen;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(
    require = @Condition(type = Condition.Type.MOD, value = "ae2")
)
@Mixin(SetProcessingPatternAmountScreen.class)
public class SetProcessingPatternAmountScreenMixin {
    @ModifyExpressionValue(method = "getMaxAmount", at = @At(value = "CONSTANT", args = "longValue=999999"))
    private long maxAmount(long amount) {
        return (long) Integer.MAX_VALUE;
    }
}
