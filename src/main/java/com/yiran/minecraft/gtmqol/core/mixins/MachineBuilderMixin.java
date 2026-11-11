package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.yiran.minecraft.gtmqol.api.generation.IDynamicGenerationHandler;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.api.machine.MachineFeatures;
import com.yiran.minecraft.gtmqol.common.modular.ModularMachines;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Shadow
    protected MachineInstanceFactory<? extends MetaMachine> instanceFactory;

    /** Every machine this builder makes gets its {@link MachineFeatures} right after it is built. */
    @Inject(method = "register()Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;", at = @At("HEAD"))
    private void gtmqol$addMachineFeatures(CallbackInfoReturnable<MachineDefinition> cir) {
        instanceFactory = MachineFeatures.wrap(instanceFactory);
    }

    // Full descriptor: there is also a synthetic register()Ljava/lang/Object; bridge from BuilderBase.
    // MultiblockMachineBuilder.register() calls super.register(), so multiblocks land here too.
    @Inject(method = "register()Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;", at = @At("RETURN"))
    private void gtmqol$queueDynamicGeneration(CallbackInfoReturnable<MachineDefinition> cir) {
        if (gtmqol$dynamicGenerated) {
            RuntimeGeneration.addMachine((MachineBuilder<?, ?, ?>) (Object) this, cir.getReturnValue());
        }
        ModularMachines.register(cir.getReturnValue());
    }
}
