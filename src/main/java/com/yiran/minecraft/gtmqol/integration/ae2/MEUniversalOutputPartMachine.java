package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeNotifiableHandler;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridge;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridges;

import appeng.api.stacks.AEKey;

import java.util.List;

/**
 * The dual output, plus a recipe handler per {@link AEStackLikeBridge} that puts its keys (e.g. chemicals) into the
 * non-item buffer. That buffer is flushed, saved and shown by key, so nothing else changes.
 */
public class MEUniversalOutputPartMachine extends MEDualOutputPartMachine {

    public MEUniversalOutputPartMachine(BlockEntityCreationInfo info) {
        super(info);
        for (var bridge : AEStackLikeBridges.all()) {
            attachTrait(new BridgedOutput<>(getNonItemBuffer(), bridge));
        }
    }

    private static class BridgedOutput<S, I> extends GenericStackLikeNotifiableHandler<S, I> {

        private final KeyStorage buffer;
        private final AEStackLikeBridge<S, I> bridge;

        BridgedOutput(KeyStorage buffer, AEStackLikeBridge<S, I> bridge) {
            super(bridge.cap(), IO.OUT);
            this.buffer = buffer;
            this.bridge = bridge;
        }

        // Like the fluid buffer: what's waiting for the network isn't recipe content.
        @Override
        public List<S> getStacks() {
            return List.of();
        }

        @Override
        protected long extract(S stack, long amount, boolean simulate) {
            return 0;
        }

        // Unbounded (up to a long), like the fluid buffer.
        @Override
        protected long insert(S stack, long amount, boolean simulate) {
            AEKey key = bridge.toKey(stack);
            if (key == null || amount <= 0) return 0;
            if (!simulate) {
                long old = buffer.storage.getOrDefault(key, 0L);
                buffer.storage.put(key, old > Long.MAX_VALUE - amount ? Long.MAX_VALUE : old + amount);
            }
            return amount;
        }

        // The buffer's callback notifies the fluid buffer's listeners and resubscribes; once per recipe is enough.
        @Override
        protected void onContentsChanged() {
            buffer.onChanged();
            notifyListeners();
        }
    }
}
