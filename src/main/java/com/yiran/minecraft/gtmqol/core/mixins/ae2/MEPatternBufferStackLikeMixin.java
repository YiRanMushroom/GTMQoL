package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridges;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLike;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLikeHolder;

import appeng.api.stacks.KeyCounter;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bridged capabilities in the pattern buffer (see {@link PatternBufferStackLike}): accept patterns with bridged
 * inputs, let a slot holding only those match recipes, and add one aggregate handler per bridge to the buffer's
 * handler list.
 */
@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class MEPatternBufferStackLikeMixin implements PatternBufferStackLikeHolder {

    @Shadow
    @Final
    private RecipeHandlerList bufferRecipeHandler;

    // List<Worker>; Worker is package-private, the field descriptor is just List
    @Shadow
    @Final
    private List<?> workers;

    @Unique
    private final List<PatternBufferStackLike.Aggregate<?, ?>> gtmqol$aggregates = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtmqol$addStackLikeHandlers(BlockEntityCreationInfo info, CallbackInfo ci) {
        var self = (MEPatternBufferPartMachine) (Object) this;
        for (var bridge : AEStackLikeBridges.all()) {
            var aggregate = self.attachTrait(PatternBufferStackLike.aggregate(bridge, this::gtmqol$slots));
            gtmqol$aggregates.add(aggregate);
            bufferRecipeHandler.addHandlers(aggregate);
        }
    }

    @Unique
    private List<MEPatternBufferPartMachine.InternalSlot> gtmqol$slots() {
        List<MEPatternBufferPartMachine.InternalSlot> slots = new ArrayList<>(workers.size());
        for (Object worker : workers) slots.add(((MEPatternBufferWorkerSlotAccessor) worker).gtmqol$getWorkerSlot());
        return slots;
    }

    @Override
    public @Nullable IRecipeHandlerTrait<?> gtmqol$getStackLikeAggregate(RecipeCapability<?> cap) {
        for (var aggregate : gtmqol$aggregates) {
            if (aggregate.getCapability() == cap) return aggregate;
        }
        return null;
    }

    @Inject(method = "onSlotChanged", at = @At("TAIL"))
    private void gtmqol$notifyStackLike(CallbackInfo ci) {
        for (var aggregate : gtmqol$aggregates) aggregate.notifyListeners();
    }

    @ModifyReturnValue(method = "checkInput", at = @At("RETURN"))
    private boolean gtmqol$acceptStackLikeInputs(boolean original,
                                                 @Local(argsOnly = true) KeyCounter[] inputHolder) {
        return original || PatternBufferStackLike.allSupported(inputHolder);
    }

    @ModifyReturnValue(method = "couldSlotMatchContents", at = @At("RETURN"))
    private static boolean gtmqol$couldMatchStackLike(boolean original,
                                                      @Local(argsOnly = true) MEPatternBufferPartMachine.InternalSlot slot,
                                                      @Local(argsOnly = true) Map<RecipeCapability<?>, List<Object>> contents) {
        return original || PatternBufferStackLike.couldMatch(slot, contents);
    }
}
