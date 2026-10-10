package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridge;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridges;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLike;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLikeSlot;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * The bridged keys of a pattern buffer slot (see {@link PatternBufferStackLike}): gtceu's {@code add} ignores keys
 * that aren't items or fluids, so take them first; the slot is only empty without them; refund and save them too.
 */
@Mixin(value = MEPatternBufferPartMachine.InternalSlot.class, remap = false)
public abstract class MEPatternBufferStackLikeSlotMixin implements PatternBufferStackLikeSlot {

    // InternalSlot is an inner class; refund needs the buffer's action source.
    @Shadow
    @Final
    MEPatternBufferPartMachine this$0;

    @Unique
    private final Object2LongOpenHashMap<AEKey> gtmqol$stackLike = new Object2LongOpenHashMap<>();

    @Unique
    private final Map<RecipeCapability<?>, IRecipeHandler<?>> gtmqol$handlers = new Reference2ObjectOpenHashMap<>();

    @Override
    public Object2LongOpenHashMap<AEKey> gtmqol$getStackLike() {
        return gtmqol$stackLike;
    }

    @Override
    public IRecipeHandler<?> gtmqol$getStackLikeHandler(AEStackLikeBridge<?, ?> bridge) {
        return gtmqol$handlers.computeIfAbsent(bridge.cap(), cap -> PatternBufferStackLike.slotHandler(this$0,
                (MEPatternBufferPartMachine.InternalSlot) (Object) this, bridge));
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void gtmqol$addStackLike(AEKey what, long amount, CallbackInfo ci) {
        if (amount > 0 && AEStackLikeBridges.forKey(what) != null) {
            gtmqol$stackLike.addTo(what, amount);
            ci.cancel();
        }
    }

    @ModifyReturnValue(method = "isEmpty", at = @At("RETURN"))
    private boolean gtmqol$emptyWithoutStackLike(boolean original) {
        return original && gtmqol$stackLike.isEmpty();
    }

    // Only reached with a network, right before the slot notifies.
    @Inject(method = "refund",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine$InternalSlot;onContentsChanged()V"))
    private void gtmqol$refundStackLike(CallbackInfo ci, @Local MEStorage networkInv, @Local IEnergyService energy) {
        PatternBufferStackLike.refund(gtmqol$stackLike, networkInv, energy, this$0.getActionSource());
    }

    // Explicit descriptors: the INBTSerializable bridge methods have the same names.
    @ModifyReturnValue(method = "serializeNBT(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;",
                       at = @At("RETURN"))
    private CompoundTag gtmqol$saveStackLike(CompoundTag tag, @Local(argsOnly = true) HolderLookup.Provider provider) {
        PatternBufferStackLike.save(gtmqol$stackLike, tag, provider);
        return tag;
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL"))
    private void gtmqol$loadStackLike(HolderLookup.Provider provider, CompoundTag tag, CallbackInfo ci) {
        PatternBufferStackLike.load(gtmqol$stackLike, tag, provider);
    }
}
