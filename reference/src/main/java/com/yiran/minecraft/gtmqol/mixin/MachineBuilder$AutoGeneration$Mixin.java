package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.yiran.minecraft.gtmqol.data.ClientDynamicModelRegisterer;
import com.yiran.minecraft.gtmqol.data.MetaMachineAutoGenerationGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MachineBuilder.class)
public class MachineBuilder$AutoGeneration$Mixin {
    @Inject(method = "register()Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;", at = @At("RETURN"))
    private void onRegister(CallbackInfoReturnable<?> cir) {
        if (MetaMachineAutoGenerationGuard.shouldAutoGenerate()) {
            ClientDynamicModelRegisterer.registerMachineAssets((MachineBuilder<?, ?>) (Object) this,
                    (MachineDefinition) cir.getReturnValue());
        }
    }
}
