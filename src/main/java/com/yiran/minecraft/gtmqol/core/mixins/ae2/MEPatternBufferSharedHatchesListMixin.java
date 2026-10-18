package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.SharedHatches;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

/**
 * {@link SharedHatches} on the pattern buffer's handler list: whatever a slot leaves of a recipe goes on to the
 * shared parts, so the slot matches if they have the rest (and both are consumed when it runs).
 */
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$BufferRecipeHandlerList",
       remap = false)
public abstract class MEPatternBufferSharedHatchesListMixin implements SharedHatches.HandlerList {

    @Shadow
    @Final
    MEPatternBufferPartMachine this$0;

    @Unique
    private List<RecipeHandlerList> gtmqol$shared = List.of();

    @Override
    public boolean gtmqol$sharesHatches() {
        return ((SharedHatches.Holder) this$0).gtmqol$sharesHatches();
    }

    @Override
    public void gtmqol$setSharedHatches(List<RecipeHandlerList> shared) {
        gtmqol$shared = shared;
    }

    @ModifyReturnValue(method = "consumeWorker", at = @At("RETURN"))
    private Map<RecipeCapability<?>, List<Object>> gtmqol$restFromSharedHatches(
            Map<RecipeCapability<?>, List<Object>> left, @Local(argsOnly = true) IO io,
            @Local(argsOnly = true) GTRecipe recipe, @Local(argsOnly = true) boolean simulate) {
        if (gtmqol$shared.isEmpty() || left.isEmpty()) return left;
        return SharedHatches.handle(gtmqol$shared, io, recipe, left, simulate);
    }
}
