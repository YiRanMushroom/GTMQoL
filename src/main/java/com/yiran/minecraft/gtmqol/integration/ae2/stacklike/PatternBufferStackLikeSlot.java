package com.yiran.minecraft.gtmqol.integration.ae2.stacklike;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;

import appeng.api.stacks.AEKey;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * Added to gtceu's pattern buffer slot ({@code MEPatternBufferPartMachine.InternalSlot}) by
 * {@code mixin.ae2.MEPatternBufferStackLikeSlotMixin}: the bridged keys pushed into the slot, next to its items and
 * fluids. See {@link PatternBufferStackLike}.
 */
public interface PatternBufferStackLikeSlot {

    /** Bridged AE keys (e.g. chemicals) in the slot, with their amounts. */
    Object2LongOpenHashMap<AEKey> gtmqol$getStackLike();

    /** The slot's recipe handler for the bridge's capability. */
    IRecipeHandler<?> gtmqol$getStackLikeHandler(AEStackLikeBridge<?, ?> bridge);
}
