package com.yiran.minecraft.gtmqol.client;

import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.machine.trait.multiblock.MultiblockFluidRendererTrait;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.yiran.minecraft.gtmqol.common.multiblock.FishingPondMachine;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.RenderTypeHelper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;

/**
 * Always-on water surface over the fishing pond's cavity. GTCEu's {@code FluidAreaRender} does the same for the
 * Large Chemical Bath, but needs a recipe-logic machine.
 */
public class FishingPondWaterRender extends DynamicRender<FishingPondMachine, FishingPondWaterRender> {

    public static final MapCodec<FishingPondWaterRender> CODEC = MapCodec.unit(FishingPondWaterRender::new);
    public static final DynamicRenderType<FishingPondMachine, FishingPondWaterRender> TYPE =
            new DynamicRenderType<>(CODEC);

    private final FluidBlockRenderer fluidRenderer = FluidBlockRenderer.Builder.create()
            .setFaceOffset(-0.125f)
            .setForcedLight(LightTexture.FULL_BRIGHT)
            .getRenderer();

    @Override
    public DynamicRenderType<FishingPondMachine, FishingPondWaterRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public boolean shouldRender(FishingPondMachine machine, Vec3 cameraPos) {
        return machine.isFormed() && super.shouldRender(machine, cameraPos);
    }

    @Override
    public void render(FishingPondMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!ConfigHolder.INSTANCE.client.renderer.renderFluids) return;
        var trait = machine.getTrait(MultiblockFluidRendererTrait.class);
        if (trait == null || !machine.isFormed() || trait.getFluidOffsets().isEmpty()) return;
        var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(
                ItemBlockRenderTypes.getRenderLayer(Fluids.WATER.defaultFluidState()), false));
        fluidRenderer.drawPlane(Direction.UP, trait.getFluidOffsets(), poseStack, consumer, Fluids.WATER,
                RenderUtil.FluidTextureType.STILL, packedOverlay, machine.getBlockPos(), machine.getLevel());
    }

    @Override
    public boolean shouldRenderOffScreen(FishingPondMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(FishingPondMachine machine) {
        return new AABB(machine.getBlockPos()).inflate(6);
    }
}
