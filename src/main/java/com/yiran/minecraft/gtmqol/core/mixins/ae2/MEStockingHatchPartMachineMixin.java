package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.yiran.minecraft.gtmqol.integration.ae2.MEUniversalInputPartMachine;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Same as {@link MEStockingBusPartMachineMixin}, for the stocking hatch. */
@Mixin(MEStockingHatchPartMachine.class)
public abstract class MEStockingHatchPartMachineMixin {

    @ModifyReturnValue(method = "testConfiguredInOtherPart", at = @At("RETURN"))
    private boolean gtmqol$checkUniversalInputs(boolean original, @Nullable GenericStack config) {
        if (original || config == null) return original;
        return MEUniversalInputPartMachine.configuredInAny((MEStockingHatchPartMachine) (Object) this, config);
    }
}
