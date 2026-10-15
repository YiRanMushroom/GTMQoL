package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.yiran.minecraft.gtmqol.integration.ae2.StockingParts;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The stocking bus's duplicate check only knows other stocking buses: also look at our stocking parts (dual and
 * universal ME inputs), so a key can't be stocked by both (it would be counted twice).
 */
@Mixin(value = MEStockingBusPartMachine.class, remap = false)
public abstract class MEStockingBusPartMachineMixin {

    @ModifyReturnValue(method = "testConfiguredInOtherPart", at = @At("RETURN"))
    private boolean gtmqol$checkOurParts(boolean original, @Nullable GenericStack config) {
        if (original || config == null) return original;
        var self = (MEStockingBusPartMachine) (Object) this;
        // the bus's own rule: a distinct bus may share keys
        if (self.isDistinct()) return false;
        return StockingParts.configuredElsewhere(self, config);
    }
}
