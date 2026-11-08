package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.client.model.machine.IControllerModelRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Formed parts of a assembling matrix drawn as the casing of its frame. A machine's own part textures are fixed per
 * definition, this frame's tier isn't. Only with AE2 loaded.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblingMatrixPartRender extends DynamicRender<MultiblockControllerMachine, AssemblingMatrixPartRender>
                              implements IControllerModelRenderer {

    public static final Codec<AssemblingMatrixPartRender> CODEC = Codec.unit(AssemblingMatrixPartRender::new);
    public static final DynamicRenderType<MultiblockControllerMachine, AssemblingMatrixPartRender> TYPE =
            new DynamicRenderType<>(CODEC);

    @Override
    public DynamicRenderType<MultiblockControllerMachine, AssemblingMatrixPartRender> getType() {
        return TYPE;
    }

    @Override
    public void render(MultiblockControllerMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    @Override
    public boolean shouldRender(MultiblockControllerMachine machine, Vec3 cameraPos) {
        return false;
    }

    @Override
    public boolean isBlockEntityRenderer() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderPartModel(List<BakedQuad> quads, MultiblockControllerMachine controller,
                                MultiblockPartMachine part, Direction frontFacing, @Nullable Direction side,
                                RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        if (!(controller instanceof AssemblingMatrixMachine matrix)) return;
        BlockState casing = matrix.casingState();
        quads.addAll(RenderUtil.getModelForState(casing).getQuads(casing, side, rand, ModelData.EMPTY, renderType));
    }
}
