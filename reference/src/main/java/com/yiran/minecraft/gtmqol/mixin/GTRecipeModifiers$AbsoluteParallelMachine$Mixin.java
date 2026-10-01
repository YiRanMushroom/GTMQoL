package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.yiran.minecraft.gtmqol.api.QoLRecipeModifiers;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(GTRecipeModifiers.class)
public class GTRecipeModifiers$AbsoluteParallelMachine$Mixin {
    @WrapMethod(method = "hatchParallel")
    private static @NotNull ModifierFunction wrapHatchParallel(MetaMachine machine, GTRecipe recipe, Operation<ModifierFunction> original) {
        if (machine.getDefinition() instanceof MultiblockMachineDefinition definition && !definition.isGenerator() && ConfigHolder.getInstance().addonConfig.parallelHatchesAreAbsolute) {
            return QoLRecipeModifiers.absoluteHatchParallel(machine, recipe);
        }

        return original.call(machine, recipe);
    }
}
