package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

/**
 * With several hatches at the highest tier, gtceu only allows one tier above it. Use the summed voltage of all
 * hatches instead (same as {@code getOverclockVoltage}), so e.g. 16 hatches can skip two tiers.
 */
@Mixin(value = WorkableElectricMultiblockMachine.class, remap = false)
public abstract class WorkableElectricMultiblockMachineMixin {

    @Shadow
    protected EnergyContainerList energyContainer;

    @Definition(id = "V", field = "Lcom/gregtechceu/gtceu/api/GTValues;V:[J")
    @Expression("return @(V[?])")
    @WrapOperation(method = "getMaxVoltage", at = @At("MIXINEXTRAS:EXPRESSION"))
    private long gtmqol$multiTierSkip(long[] array, int index, Operation<Long> original) {
        if (!GTMQoLConfig.get().overclocking.enableMultiTierSkipping) {
            return original.call(array, index);
        }
        long voltage = energyContainer.getInputVoltage();
        long amperage = energyContainer.getInputAmperage();
        return amperage == 1 ? GTValues.VEX[GTUtil.getFloorTierByVoltage(voltage)] : voltage;
    }
}
