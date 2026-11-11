package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;
import com.gregtechceu.gtceu.client.renderer.BlockEntityWithBERModelRenderer;

import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * NeoForge asks the renderer, not the block entity, for the box a block entity is drawn in (frustum culling), and
 * gtceu's renderer doesn't answer: the boxes of its models (a multiblock's fluid surface, the fusion ring, the
 * assembly line's items, our fishing pond's water) were never used on 1.21, so all of these vanished as soon as the
 * controller's own block was out of view. On 1.20.1 {@code MetaMachine.getRenderBoundingBox()} still does it.
 * Remove once gtceu's renderer forwards the call.
 */
@Mixin(value = BlockEntityWithBERModelRenderer.class, remap = false)
public abstract class BlockEntityWithBERModelRendererMixin<T extends BlockEntity> implements BlockEntityRenderer<T> {

    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderDispatcher;

    // Untested: the Industrial Fishing Pond's water (and a fusion reactor's ring) stays visible while the controller
    // is outside the view and the rest of the structure is inside
    @SuppressWarnings("unchecked")
    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        BakedModel model = blockRenderDispatcher.getBlockModel(blockEntity.getBlockState());
        if (model instanceof IBlockEntityRendererBakedModel<?> berModel &&
                berModel.getBlockEntityType() == blockEntity.getType()) {
            return ((IBlockEntityRendererBakedModel<T>) berModel).getRenderBoundingBox(blockEntity);
        }
        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
    }
}
