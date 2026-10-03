package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.yiran.minecraft.gtmqol.api.generation.IDynamicGenerationHandler;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MachineBuilder.class, remap = false)
public abstract class MachineBuilderMixin implements IDynamicGenerationHandler {

    @Unique
    private boolean gtmqol$dynamicGenerated;

    @Override
    public void gtmqol$setDynamicallyGenerated(boolean dynamicGenerated) {
        this.gtmqol$dynamicGenerated = dynamicGenerated;
    }

    @Override
    public boolean gtmqol$isDynamicallyGenerated() {
        return gtmqol$dynamicGenerated;
    }

    // Full descriptor: there is also a synthetic bridge from Registrate's AbstractBuilder.register().
    // MultiblockMachineBuilder doesn't override register(), so multiblocks land here too.
    @Inject(method = "register()Lcom/gregtechceu/gtceu/api/registry/registrate/entry/MachineEntry;",
            at = @At("RETURN"))
    private void gtmqol$queueDynamicGeneration(CallbackInfoReturnable<MachineEntry<?>> cir) {
        if (gtmqol$dynamicGenerated) {
            RuntimeGeneration.addMachine((MachineBuilder<?, ?, ?>) (Object) this, cir.getReturnValue());
        }
    }
}
