package com.yiran.minecraft.gtmqol.mixin.gtmthings;

import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(
        require = @Condition(
                value = "gtmthings",
                type = Condition.Type.MOD
        ))
@Mixin(WirelessEnergyContainer.class)
public class WirelessEnergyContainerMixin {
    @ModifyExpressionValue(method = {"addEnergy", "removeEnergy"}, at = @At(value = "FIELD", target = "Lcom/hepdd/gtmthings/config/ConfigHolder;isWirelessRateEnable:Z", opcode = Opcodes.GETFIELD), remap = false)
    private boolean modifyRateEnable(boolean original) {
        return original && !ConfigHolder.getInstance().addonConfig.enableGTMThingsIntegration;
    }
}
