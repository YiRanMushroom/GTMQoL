package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

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

    @WrapMethod(method = "clear", remap = false)
    private boolean gtmqol$lockClear(Operation<Boolean> original) {
        synchronized (this) {
            return original.call();
        }
    }
}
