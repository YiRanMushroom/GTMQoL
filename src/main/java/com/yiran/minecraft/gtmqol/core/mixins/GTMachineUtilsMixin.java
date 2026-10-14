package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GTMachineUtils.class, remap = false)
public class GTMachineUtilsMixin {

    // Large boilers: the muffler and the maintenance hatch (the only two setExactLimit calls in the pattern lambda)
    // become optional, so the large boilers can be built before LV.
    @WrapOperation(method = "/^lambda\\$registerLargeBoiler\\$/",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/multiblock/MultiPredicate;setExactLimit(I)Lcom/gregtechceu/gtceu/api/multiblock/MultiPredicate;"),
                   require = 2)
    private static MultiPredicate gtmqol$optionalBoilerParts(MultiPredicate predicate, int limit,
                                                             Operation<MultiPredicate> original) {
        if (!GTMQoLConfig.get().steamTweaks.optionalLargeBoilerParts) return original.call(predicate, limit);
        return predicate.setMaxGlobalLimited(limit);
    }
}
