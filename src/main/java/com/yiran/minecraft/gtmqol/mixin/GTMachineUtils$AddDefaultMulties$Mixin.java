package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.yiran.minecraft.gtmqol.mixin_impl.AddDefaultMultiesImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

@Mixin(GTMachineUtils.class)
public class GTMachineUtils$AddDefaultMulties$Mixin {
    @Inject(method = "registerTieredMachines(Lcom/gregtechceu/gtceu/api/registry/registrate/GTRegistrate;Ljava/lang/String;Ljava/util/function/BiFunction;Ljava/util/function/BiFunction;[I)[Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;",
            at = @At("RETURN"),
            remap = false)
    private static void $addDefaultMulties(GTRegistrate registrate, String name, BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory, BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder, int[] tiers, CallbackInfoReturnable<MachineDefinition[]> cir) {
        Arrays.stream(cir.getReturnValue())
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(definition ->
                        AddDefaultMultiesImpl.generateMultiblockForSimpleMachine(
                                registrate, name, definition.getRecipeTypes()));
    }
}
