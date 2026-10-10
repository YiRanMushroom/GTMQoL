package com.yiran.minecraft.gtmqol.integration.ae2.stacklike;

import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeRecipeCapability;

import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.Nullable;

/**
 * Converts the stacks of a {@link GenericStackLikeRecipeCapability} to and from the AE keys of the mod that puts
 * them in ME networks (e.g. Applied Mekanistics for chemicals).
 * <p>
 * The direction is capability to AE: a capability opts into AE support with a bridge, and the ME parts only accept
 * keys that items, fluids or a bridged capability cover. There is deliberately no recipe capability for AE keys as
 * such, so whatever goes into a recipe has a real capability that other handlers (e.g. a mod's own hatches) can
 * implement too.
 */
public interface AEStackLikeBridge<S, I> {

    GenericStackLikeRecipeCapability<S, I> cap();

    /** Whether the key is one of this capability's stacks. */
    boolean isKey(AEKey key);

    /** @return null if the stack has no AE key (e.g. empty) */
    @Nullable
    AEKey toKey(S stack);

    /** Only called with keys {@link #isKey} accepts. */
    S fromKey(AEKey key, long amount);
}
