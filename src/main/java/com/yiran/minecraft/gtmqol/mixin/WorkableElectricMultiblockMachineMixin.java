package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorkableElectricMultiblockMachine.class)
public abstract class WorkableElectricMultiblockMachineMixin {
    @Shadow
    public abstract long getOverclockVoltage();

    @WrapMethod(method = "getMaxVoltage")
    public long getMaxVoltage(Operation<Long> original) {
        if (ConfigHolder.instance.overclockingConfig.singleEnergyHatchTierSkipping) {
            return this.getOverclockVoltage();
        }

        return original.call();
    }
}
