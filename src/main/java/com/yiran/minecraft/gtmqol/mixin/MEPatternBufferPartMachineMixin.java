package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.ae2.AbstractMEPatternBufferPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * GTCEu's MAX_PATTERN_COUNT is a compile-time constant, so javac inlines 27 wherever it's used. Replace it for
 * {@link AbstractMEPatternBufferPartMachine}: pattern inventory, internal slots and pattern details in the
 * constructor, and the worker cap in syncWorkerCount / addWorker. The UI and the terminal inventory are
 * overridden in the subclass instead.
 */
@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class MEPatternBufferPartMachineMixin {

    @ModifyConstant(method = { "<init>", "syncWorkerCount", "addWorker" },
                    constant = @Constant(intValue = 27),
                    require = 5)
    private int gtmqol$maxPatternCount(int original) {
        return (Object) this instanceof AbstractMEPatternBufferPartMachine buffer ?
                buffer.getMaxPatternCount() : original;
    }
}
