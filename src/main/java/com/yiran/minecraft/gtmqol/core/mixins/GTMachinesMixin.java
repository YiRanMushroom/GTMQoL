package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

/**
 * Drops GTCEu's output limit on the steam macerator so it keeps all of a recipe's outputs (byproducts) and gets the
 * slots for them. It is the only call left in the class (the LV-HV macerators lost theirs upstream), in a builder
 * lambda of a static field; GTMultiMachinesMixin does the same for the steam grinder and steam oven.
 */
@Mixin(value = GTMachines.class, remap = false)
public class GTMachinesMixin {

    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "/^lambda\\$static\\$/",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/registry/registrate/MachineBuilder;addOutputLimit(Lcom/gregtechceu/gtceu/api/capability/recipe/RecipeCapability;I)Lcom/gregtechceu/gtceu/api/registry/registrate/MachineBuilder;"),
                   require = 1)
    private static MachineBuilder gtmqol$noOutputLimit(MachineBuilder builder, RecipeCapability<?> cap, int limit,
                                                       Operation<MachineBuilder> original) {
        if (!GTMQoLConfig.get().steamTweaks.noOutputLimits) return original.call(builder, cap, limit);
        return builder;
    }
}
