package com.yiran.minecraft.gtmqol.mixin.recipedb;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$Worker", remap = false)
public interface MEPatternBufferWorkerAccessor {

    @Accessor("slot")
    MEPatternBufferPartMachine.InternalSlot gtmqol$getSlot();
}
