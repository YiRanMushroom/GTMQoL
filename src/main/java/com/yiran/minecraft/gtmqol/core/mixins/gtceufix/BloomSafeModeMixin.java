package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;

import net.minecraft.core.SectionPos;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * gtceu 1.21 bug: without Sodium/Embeddium, block bloom (glowing overlays) is kept per chunk section in buffers of its
 * own, outside the chunk mesh. A section's buffer is only replaced when its rebuild has bloom faces, so once the last
 * glowing face of a section is gone (e.g. a hatch broken) the old buffer keeps drawing until the chunk or the level
 * unloads; F3+A doesn't clear it either. Drop the old buffer when a rebuild had none. Remove once gtceu fixes it.
 */
@Mixin(targets = "com.gregtechceu.gtceu.client.bloom.BloomRenderer$SafeMode", remap = false)
public abstract class BloomSafeModeMixin {

    /** The section the rebuild running on this thread has written bloom faces for. */
    @Unique
    private static final ThreadLocal<SectionPos> gtmqol$bloomSection = new ThreadLocal<>();

    @Shadow
    static void invalidateSectionData(SectionPos sectionPos) {
        throw new AssertionError();
    }

    @Inject(method = "getOrStartBloomBuffer", at = @At("HEAD"))
    private static void gtmqol$markBloom(SectionPos sectionPos, CallbackInfoReturnable<BufferBuilder> cir) {
        gtmqol$bloomSection.set(sectionPos);
    }

    // Called with a started buffer by gtceu's section geometry renderer, and at the end of every rebuild, when the
    // buffer is baked already or was never started.
    @Inject(method = "bakeBloomChunkBuffers", at = @At("HEAD"))
    private static void gtmqol$dropStale(SectionPos sectionPos, VertexSorting vertexSorting, CallbackInfo ci) {
        if (BloomRenderer.SafeMode.BLOOM_BUFFER_BUILDERS.containsKey(sectionPos)) return;
        boolean hadBloom = sectionPos.equals(gtmqol$bloomSection.get());
        gtmqol$bloomSection.remove();
        if (!hadBloom && BloomRenderer.SafeMode.BLOOM_BUFFERS.containsKey(sectionPos)) {
            invalidateSectionData(sectionPos);
        }
    }
}
