package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.AABB;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * gtceu 1.21 bug: the in-world highlight of a multiblock's missing part (the button under "Invalid structure") passes
 * its color as 0-255 ints to {@code LevelRenderer.renderLineBox}, which takes 0-1 floats and multiplies them by 255.
 * Cut to a byte, alpha 255 becomes 1, so the box was invisible. Scale them back to 0-1 when any is above 1. Remove
 * once gtceu fixes it.
 */
@Mixin(targets = "com.gregtechceu.gtceu.client.renderer.AABBHighlightRenderer$AABBHighlight", remap = false)
public class AABBHighlightColorMixin {

    @WrapOperation(method = "render",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLineBox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/AABB;FFFF)V"))
    private void gtmqol$colorAsFraction(PoseStack poseStack, VertexConsumer buffer, AABB box, float red, float green,
                                        float blue, float alpha, Operation<Void> original) {
        // only while gtceu still passes 0-255, so a fixed gtceu isn't scaled twice
        if (red > 1 || green > 1 || blue > 1 || alpha > 1) {
            original.call(poseStack, buffer, box, red / 255f, green / 255f, blue / 255f, alpha / 255f);
        } else {
            original.call(poseStack, buffer, box, red, green, blue, alpha);
        }
    }
}
