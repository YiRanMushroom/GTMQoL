package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.integration.ae2.AbstractMEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.PatternOutputMarks;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** A slot of our pattern buffers marks its pattern's main output, see {@link PatternOutputMarks}. */
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$BufferRecipeHandlerList",
       remap = false)
public abstract class MEPatternBufferOutputMarksMixin {

    @Shadow
    @Final
    MEPatternBufferPartMachine this$0;

    @Inject(method = "handlersFor", at = @At("HEAD"), cancellable = true)
    private void gtmqol$patternOutputMark(RecipeCapability<?> cap, @Coerce Object worker,
                                          CallbackInfoReturnable<List<IRecipeHandler<?>>> cir) {
        if (cap != GhostIngredientRecipeCapability.CAP) return;
        if (!(this$0 instanceof AbstractMEPatternBufferPartMachine buffer) || !buffer.marksPatternOutputs()) return;
        GhostIngredient<?> mark = PatternOutputMarks.of(
                ((MEPatternBufferWorkerPatternAccessor) worker).gtmqol$getWorkerPattern());
        cir.setReturnValue(mark == null ? List.of() : List.of(new PatternOutputMarks(() -> List.of(mark))));
    }
}
