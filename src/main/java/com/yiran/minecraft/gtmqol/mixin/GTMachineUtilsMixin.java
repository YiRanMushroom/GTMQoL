package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.yiran.minecraft.gtmqol.modular.ModularMachines;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;

/** Every tiered single-block machine (gtceu's, ours, other addons') goes through this. */
@Mixin(value = GTMachineUtils.class, remap = false)
public class GTMachineUtilsMixin {

    @Inject(method = "registerTieredMachines", at = @At("RETURN"))
    private static void gtmqol$registerModular(GTRegistrate registrate, String name,
                                               MachineInstanceFactory.Tiered<?> factory,
                                               BiFunction<Integer, MachineBuilder<MachineDefinition, ?, ?>, MachineDefinition> builder,
                                               int[] tiers, CallbackInfoReturnable<MachineDefinition[]> cir) {
        ModularMachines.register(name, cir.getReturnValue());
    }
}
