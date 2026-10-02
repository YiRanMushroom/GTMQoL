package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import java.util.function.Supplier;

@Mixin(value = MachineBuilder.class, remap = false)
public interface MachineBuilderAccessor {

    /** Recipe types are only resolved when the definition is created, in the machine registry event. */
    @Accessor("unresolvedRecipeTypes")
    Set<Supplier<GTRecipeType>> gtmqol$getUnresolvedRecipeTypes();
}
