package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The ME output hatch's buffer (private in gtceu), for {@link MEOutputHatchBufferMixin}. */
@Mixin(value = MEOutputHatchPartMachine.class, remap = false)
public interface MEOutputHatchPartMachineAccessor {

    @Accessor("internalBuffer")
    KeyStorage gtmqol$getInternalBuffer();
}
