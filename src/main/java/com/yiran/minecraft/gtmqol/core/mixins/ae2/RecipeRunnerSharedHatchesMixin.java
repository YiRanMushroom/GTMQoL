package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeRunner;
import com.yiran.minecraft.gtmqol.integration.ae2.SharedHatches;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

/** Hands pattern buffers the shared parts of the machine running the recipe, see {@link SharedHatches}. */
@Mixin(value = RecipeRunner.class, remap = false)
public abstract class RecipeRunnerSharedHatchesMixin {

    @Shadow
    @Final
    private Map<IO, List<RecipeHandlerList>> capabilityProxies;

    @WrapOperation(method = "handleContents",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeHandlerList;handleRecipe(Lcom/gregtechceu/gtceu/api/capability/recipe/IO;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Ljava/util/Map;Z)Ljava/util/Map;"))
    private Map<RecipeCapability<?>, List<Object>> gtmqol$withSharedHatches(
            RecipeHandlerList handler, IO io, GTRecipe recipe, Map<RecipeCapability<?>, List<Object>> contents,
            boolean simulate, Operation<Map<RecipeCapability<?>, List<Object>>> original) {
        if (!(handler instanceof SharedHatches.HandlerList buffer) || !buffer.gtmqol$sharesHatches()) {
            return original.call(handler, io, recipe, contents, simulate);
        }
        buffer.gtmqol$setSharedHatches(SharedHatches.of(capabilityProxies.getOrDefault(io, List.of()), handler));
        try {
            return original.call(handler, io, recipe, contents, simulate);
        } finally {
            buffer.gtmqol$setSharedHatches(List.of());
        }
    }
}
