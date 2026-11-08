package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.yiran.minecraft.gtmqol.integration.ae2.crafting.PatternBatch;

import appeng.api.crafting.IPatternDetails;
import appeng.me.service.CraftingService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Who can do a batch: who offers its pattern. Patterns are the keys here, and batches are nobody's. */
@Mixin(value = CraftingService.class, remap = false)
public abstract class CraftingServiceBatchMixin {

    @ModifyVariable(method = "getProviders", at = @At("HEAD"), argsOnly = true)
    private IPatternDetails gtmqol$patternOfBatch(IPatternDetails key) {
        return PatternBatch.unwrap(key);
    }
}
