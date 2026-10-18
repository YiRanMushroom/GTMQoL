package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.client.renderer.AABBHighlightRenderer;
import com.yiran.minecraft.gtmqol.client.GTMQoLRenderTypes;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * gtceu 1.21 bug: the missing-part highlight is meant to show through blocks, but it only turns depth testing off with
 * {@code RenderSystem} and then adds its lines to the shared {@code RenderType.lines()} buffer, which is drawn later
 * with its own depth test. Draw the lines with {@link GTMQoLRenderTypes#SEE_THROUGH_LINES}, right away, in the stage
 * gtceu renders them in (1.20.1's gtceu does the same with its own quads). Remove once gtceu fixes it.
 */
@Mixin(value = AABBHighlightRenderer.class, remap = false)
public class AABBHighlightSeeThroughMixin {

    @WrapOperation(method = "tick",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer gtmqol$seeThroughLines(MultiBufferSource.BufferSource bufferSource, RenderType type,
                                                  Operation<VertexConsumer> original) {
        return original.call(bufferSource, type == RenderType.lines() ? GTMQoLRenderTypes.SEE_THROUGH_LINES : type);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void gtmqol$drawNow(PoseStack stack, MultiBufferSource.BufferSource bufferSource, Camera camera,
                                CallbackInfo ci) {
        bufferSource.endBatch(GTMQoLRenderTypes.SEE_THROUGH_LINES);
    }
}
