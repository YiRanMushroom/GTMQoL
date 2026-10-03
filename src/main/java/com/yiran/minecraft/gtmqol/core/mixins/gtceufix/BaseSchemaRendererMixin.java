package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import brachy.modularui.drawable.schema.BaseSchemaRenderer;
import brachy.modularui.drawable.schema.Viewport;
import brachy.modularui.screen.viewport.GuiContext;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * ModularUI bug: the schema (multiblock preview) GL viewport is placed with ModularUI's own transform only, which
 * ignores the pose translation EMI/JEI apply when drawing an embedded recipe UI, so the preview ends up at the
 * screen's top-left. Place it with the actual graphics pose instead (in a normal ModularUI screen the pose is
 * exactly ModularUI's transform, so nothing changes there). Remove once ModularUI fixes it.
 */
@Mixin(value = BaseSchemaRenderer.class, remap = false)
public class BaseSchemaRendererMixin {

    @WrapOperation(method = "draw",
                   at = @At(value = "INVOKE",
                            target = "Lbrachy/modularui/drawable/schema/Viewport;calculateOpenGLViewportFromRectangle(IIII)V"))
    private void gtmqol$placeByGraphicsPose(Viewport viewport, int transformX, int transformY, int width, int height,
                                            Operation<Void> original,
                                            @Local(argsOnly = true) GuiContext context,
                                            @Local(argsOnly = true, ordinal = 0) int x,
                                            @Local(argsOnly = true, ordinal = 1) int y) {
        Vector3f pos = context.getLastGraphicsPose().transformPosition(x, y, 0, new Vector3f());
        original.call(viewport, Math.round(pos.x), Math.round(pos.y), width, height);
    }
}
