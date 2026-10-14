package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.integration.kjs.builders.machine.KJSTieredMachineBuilder;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import com.yiran.minecraft.gtmqol.functionality.AddModularMultiblocksLogic;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;

@Mixin(value = KJSTieredMachineBuilder.class, remap = false)
public abstract class KJSTieredMachineBuilder$GenDefaultMulti$Mixin extends BuilderBase<MachineDefinition[]> {

    public KJSTieredMachineBuilder$GenDefaultMulti$Mixin(ResourceLocation id) {
        super(id);
    }

    @Inject(method = "register()[Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;", at = @At("RETURN"))
    private void registerModularMachine(CallbackInfoReturnable<MachineDefinition[]> cir) {
        if (!ConfigHolder.getInstance().addonConfig.registerModularMachinesForSimpleMachines) {
            return;
        }

        MachineDefinition[] definitions = cir.getReturnValue();
        if (definitions == null) return;

        Arrays.stream(definitions)
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(definition -> AddModularMultiblocksLogic.generateMultiblockForSimpleMachine(
                        GTRegistration.REGISTRATE,
                        this.id.getPath(),
                        definition.getRecipeTypes(),
                        definition
                ));
    }
}