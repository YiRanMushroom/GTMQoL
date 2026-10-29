package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

import brachy.modularui.drawable.schema.DummyChunkSource;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

/**
 * ModularUI bug: the multiblock preview builds its mesh on background threads ({@code BaseSchemaRenderer.recompile};
 * a cancelled build may still be running when the next one starts), but its dummy level keeps chunks in a plain
 * {@code Long2ObjectOpenHashMap}. Two threads adding chunks at once broke the map while it grew and crashed the game
 * ("Batching chunks", {@code ArrayIndexOutOfBoundsException} in {@code computeIfAbsent}). Lock the map's users on
 * the chunk source. Remove once ModularUI fixes it.
 * <p>
 * Unlike 1.21.1's, the 1.20.1 ModularUI (builds 23 to 25) has no {@code clear()}, so only {@code getChunk} is locked
 * here (a wrapper for the missing method fails to apply and stops the game at startup).
 * Untested: the client starts without a mixin error for DummyChunkSourceMixin, and opening a multiblock preview
 * (terminal or EMI multiblock page) still renders.
 */
@Mixin(DummyChunkSource.class)
public class DummyChunkSourceMixin {

    @WrapMethod(method = "getChunk")
    private ChunkAccess gtmqol$lockGetChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load,
                                            Operation<ChunkAccess> original) {
        synchronized (this) {
            return original.call(chunkX, chunkZ, requiredStatus, load);
        }
    }
}
