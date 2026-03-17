package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorkableElectricMultiblockMachine.class)
public class WorkableElectricMultiblock$MultiTierSkip$Mixin {
    @Shadow
    protected EnergyContainerList energyContainer;

    @Definition(id = "V", field = "Lcom/gregtechceu/gtceu/api/GTValues;V:[J")
    @Expression("return @(V[?])")
    @WrapOperation(method = "getMaxVoltage", at = @At("MIXINEXTRAS:EXPRESSION"))
    public long getMaxVoltageIfMultiHatch(long[] array, int index, Operation<Long> original) {
        long voltage = this.energyContainer.getInputVoltage();
        long amperage = this.energyContainer.getInputAmperage();
        return amperage == 1L ? GTValues.VEX[GTUtil.getFloorTierByVoltage(voltage)] : voltage;
    }
}
