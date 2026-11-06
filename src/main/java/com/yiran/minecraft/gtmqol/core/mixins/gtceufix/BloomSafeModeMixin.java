package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.yiran.minecraft.gtmqol.client.BloomRebuild;

import net.minecraft.core.SectionPos;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Two gtceu 1.21 bugs without Sodium/Embeddium, where block bloom (glowing overlays) is kept per chunk section in
 * buffers of its own, outside the chunk mesh. Remove once gtceu fixes them.
 *
 * <p>All rebuilds of a section shared one builder. Two can overlap, a cancelled one keeps running, and the first to
 * end baked the builder under the other, which crashed the game with {@code Not building!} or an
 * {@code IndexOutOfBoundsException} when sorting the quads. So each rebuild writes to a builder of its own, kept per
 * thread: a rebuild stays on one. {@code BLOOM_BUFFER_BUILDERS} stays empty.</p>
 *
 * <p>A section's buffer was only replaced when its rebuild had bloom faces, so once the last glowing face of a section
 * was gone (e.g. a hatch broken) the old buffer kept drawing until the chunk or the level unloaded; F3+A didn't clear
 * it either. So the old buffer is dropped when a rebuild had none.</p>
 */
@Mixin(targets = "com.gregtechceu.gtceu.client.bloom.BloomRenderer$SafeMode", remap = false)
public abstract class BloomSafeModeMixin {

    @Unique
    private static final ThreadLocal<BloomRebuild> gtmqol$rebuild = new ThreadLocal<>();

    @Shadow
    static void invalidateSectionData(SectionPos sectionPos) {
        throw new AssertionError();
    }

    @Shadow
    private static void finishBloomBuffer(SectionPos sectionPos, MeshData mesh, BufferBuilder builder,
                                          VertexSorting vertexSorting) {
        throw new AssertionError();
    }

    @Inject(method = "getOrStartBloomBuffer", at = @At("HEAD"), cancellable = true)
    private static void gtmqol$ownBuffer(SectionPos sectionPos, CallbackInfoReturnable<BufferBuilder> cir) {
        BloomRebuild rebuild = gtmqol$rebuild.get();
        // another section: the last rebuild on this thread threw before baking
        if (rebuild == null || !rebuild.section().equals(sectionPos)) {
            var bytes = new ByteBufferBuilder(GTRenderTypes.bloom().bufferSize());
            var builder = new BufferBuilder(bytes, GTRenderTypes.bloom().mode(), GTRenderTypes.bloom().format());
            rebuild = new BloomRebuild(sectionPos, bytes, builder);
            gtmqol$rebuild.set(rebuild);
        }
        cir.setReturnValue(rebuild.builder());
    }

    // Untested: no more "Not building!" or sortQuads crashes, glowing blocks still glow, and stop glowing when broken
    @Inject(method = "bakeBloomChunkBuffers", at = @At("HEAD"), cancellable = true)
    private static void gtmqol$bakeOwnBuffer(SectionPos sectionPos, VertexSorting vertexSorting, CallbackInfo ci) {
        ci.cancel();
        BloomRebuild rebuild = gtmqol$rebuild.get();
        gtmqol$rebuild.remove();
        MeshData mesh = rebuild != null && rebuild.section().equals(sectionPos) ? rebuild.builder().build() : null;
        if (mesh == null) {
            if (BloomRenderer.SafeMode.BLOOM_BUFFERS.containsKey(sectionPos)) invalidateSectionData(sectionPos);
            return;
        }
        // finishBloomBuffer takes the bytes from this map to sort the quads
        synchronized (BloomRenderer.SafeMode.BLOOM_BYTE_BUFFERS) {
            BloomRenderer.SafeMode.BLOOM_BYTE_BUFFERS.put(sectionPos, rebuild.bytes());
            finishBloomBuffer(sectionPos, mesh, rebuild.builder(), vertexSorting);
        }
    }
}
