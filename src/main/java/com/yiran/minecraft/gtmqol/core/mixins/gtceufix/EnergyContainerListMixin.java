package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.misc.EnergyContainerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * gtceu performance bug: {@code EnergyContainerList.hasPrimeFactorGreaterThanTwo} trial-divides up to {@code l / 2}
 * (the bound never shrinks), so it is O(amperage). The list is rebuilt on every {@code getEnergyContainer()}, and
 * the multiblock UI's energy usage line calls that every tick through a sync value; with a large total amperage
 * (wireless hatches) that was 77% of the profile. "Has a prime factor greater than two" is just "is not a power of
 * two", which is O(1). The original also returns false for odd primes (3, 5, 7...), but the caller falls through
 * to the same {@code amperage = 1} for them, so {@code calculateVoltageAmperage} gives the same results. Remove
 * once gtceu fixes it.
 */
@Mixin(value = EnergyContainerList.class, remap = false)
public abstract class EnergyContainerListMixin {

    @Inject(method = "hasPrimeFactorGreaterThanTwo", at = @At("HEAD"), cancellable = true)
    private static void gtmqol$notPowerOfTwo(long l, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(l > 1 && (l & (l - 1)) != 0);
    }
}
