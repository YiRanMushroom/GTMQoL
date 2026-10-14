package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

/**
 * Drops GTCEu's output limits so machines keep all of a recipe's outputs (and get the slots for them): the steam
 * macerator and the LV-HV macerators (byproducts). These are the only two calls in the class, both in the builder
 * lambdas of static fields; GTMultiMachinesMixin does the same for the steam grinder and steam oven.
 */
@Mixin(value = GTMachines.class, remap = false)
public class GTMachinesMixin {

    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "/^lambda\\$static\\$/",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/registry/registrate/builder/MachineBuilder;addOutputLimit(Lcom/gregtechceu/gtceu/api/capability/recipe/RecipeCapability;I)Lcom/gregtechceu/gtceu/api/registry/registrate/builder/MachineBuilder;"),
                   require = 2)
    private static MachineBuilder gtmqol$noOutputLimit(MachineBuilder builder, RecipeCapability<?> cap, int limit,
                                                       Operation<MachineBuilder> original) {
        if (!GTMQoLConfig.get().steamTweaks.noOutputLimits) return original.call(builder, cap, limit);
        return builder;
    }
}
