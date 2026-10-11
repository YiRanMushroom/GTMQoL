package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridges;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLike;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLikeSlot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * The pattern buffer's handler list runs a recipe on one slot at a time; bridged capabilities use that slot's
 * handler (see {@link PatternBufferStackLike}), and a slot holding only bridged keys isn't skipped as empty.
 */
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$BufferRecipeHandlerList",
       remap = false)
public abstract class MEPatternBufferRecipeHandlerListMixin {

    // `slot.isItemEmpty() && slot.isFluidEmpty()`: empty only without bridged keys too.
    @WrapOperation(method = "handleRecipe",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine$InternalSlot;isFluidEmpty()Z"))
    private boolean gtmqol$emptyWithoutStackLike(MEPatternBufferPartMachine.InternalSlot slot,
                                                 Operation<Boolean> original) {
        return original.call(slot) && ((PatternBufferStackLikeSlot) slot).gtmqol$getStackLike().isEmpty();
    }

    // Worker is package-private: @Coerce it to Object and read its slot through our accessor.
    @Inject(method = "handlersFor", at = @At("HEAD"), cancellable = true)
    private void gtmqol$stackLikeHandlers(RecipeCapability<?> cap, @Coerce Object worker,
                                          CallbackInfoReturnable<List<IRecipeHandler<?>>> cir) {
        var bridge = AEStackLikeBridges.forCap(cap);
        if (bridge == null) return;
        var slot = (PatternBufferStackLikeSlot) ((MEPatternBufferWorkerSlotAccessor) worker).gtmqol$getWorkerSlot();
        cir.setReturnValue(List.of(slot.gtmqol$getStackLikeHandler(bridge)));
    }
}
