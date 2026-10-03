package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.common.modular.ModularMachines;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;

/** Every tiered single-block machine (gtceu's, ours, other addons') goes through this. */
@Mixin(value = GTMachineUtils.class, remap = false)
public class GTMachineUtilsMixin {

    // Large boilers: the muffler and the maintenance hatch (the only two setExactLimit calls in the pattern lambda)
    // become optional, so the large boilers can be built before LV.
    @WrapOperation(method = "/^lambda\\$registerLargeBoiler\\$/",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/multiblock/MultiPredicate;setExactLimit(I)Lcom/gregtechceu/gtceu/api/multiblock/MultiPredicate;"),
                   require = 2)
    private static MultiPredicate gtmqol$optionalBoilerParts(MultiPredicate predicate, int limit,
                                                             Operation<MultiPredicate> original) {
        if (!GTMQoLConfig.get().steamTweaks.optionalLargeBoilerParts) return original.call(predicate, limit);
        return predicate.setMaxGlobalLimited(limit);
    }

    @Inject(method = "registerTieredMachines", at = @At("RETURN"))
    private static void gtmqol$registerModular(GTRegistrate registrate, String name,
                                               MachineInstanceFactory.Tiered<?> factory,
                                               BiFunction<Integer, MachineBuilder<MachineDefinition, ?, ?>, MachineDefinition> builder,
                                               int[] tiers, CallbackInfoReturnable<MachineDefinition[]> cir) {
        ModularMachines.register(name, cir.getReturnValue());
    }
}
