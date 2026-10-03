package com.yiran.minecraft.gtmqol.core.mixins.recipedb;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public interface MEPatternBufferPartMachineAccessor {

    /** Elements are the package-private {@code Worker}, read them with {@link MEPatternBufferWorkerAccessor}. */
    @Accessor("workers")
    List<?> gtmqol$getWorkers();
}
