package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MachineBuilder.class)
public interface IMachineBuilderAccessor {
    @Accessor("model")
    MachineBuilder.ModelInitializer getModelInitializer();
}
