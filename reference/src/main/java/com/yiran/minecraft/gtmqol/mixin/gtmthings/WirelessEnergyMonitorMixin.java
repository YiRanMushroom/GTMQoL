package com.yiran.minecraft.gtmqol.mixin.gtmthings;

import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;

@Restriction(
        require = @Condition(
                value = "gtmthings",
                type = Condition.Type.MOD
        ))
@Mixin(WirelessEnergyMonitor.class)
public class WirelessEnergyMonitorMixin {
//    @ModifyExpressionValue(method = {"addDisplayText"}, at = @At(value = "FIELD", target = "Lcom/hepdd/gtmthings/config/ConfigHolder;isWirelessRateEnable:Z", opcode = Opcodes.GETFIELD), remap = false)
//    private boolean modifyRateEnable(boolean original) {
//        return original && !ConfigHolder.getInstance().addonConfig.enableGTMThingsIntegration;
//    }
}
