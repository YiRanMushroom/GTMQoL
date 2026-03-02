package com.yiran.minecraft.gtmqol.mixin.gtmthings;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.hepdd.gtmthings.common.registry.GTMTRegistration;
import com.hepdd.gtmthings.data.WirelessMachines;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import com.yiran.minecraft.gtmqol.gtmthings.WirelessEnergyAccessor;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Condition(
        value = "gtmthings",
        type = Condition.Type.MOD
)
@Mixin(WirelessMachines.class)
public class WirelessMachinesMixin {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onStaticInit(CallbackInfo ci) {
        if (ConfigHolder.getInstance().addonConfig.enableGTMThingsIntegration)
            WirelessEnergyAccessor.INSTANCE = GTMTRegistration.GTMTHINGS_REGISTRATE.machine("wireless_energy_accessor", WirelessEnergyAccessor::new)
                    .rotationState(RotationState.ALL).overlayTieredHullModel("energy_output_hatch").tier(5).register();
    }
}
