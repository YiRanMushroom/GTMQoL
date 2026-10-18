package com.yiran.minecraft.gtmqol.client;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.client.model.machine.IControllerModelRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The controller and formed parts drawn as the casing block itself, with its model data worked out at their
 * position, instead of gtceu's plain casing texture. For casings with their own connected textures (Create's), so
 * they join the casings around them. Parts like gtceu's {@code BoilerMultiPartRender} without the firebox; the
 * controller's own casing faces must be see-through, see {@code MultiblockShape#casingParts}.
 */
public class CasingPartRender extends DynamicRender<MultiblockControllerMachine, CasingPartRender>
                              implements IControllerModelRenderer {

    public static final MapCodec<CasingPartRender> CODEC = BlockState.CODEC.fieldOf("casing")
            .xmap(CasingPartRender::new, render -> render.casing);
    public static final DynamicRenderType<MultiblockControllerMachine, CasingPartRender> TYPE =
            new DynamicRenderType<>(CODEC);

    private final BlockState casing;
    private @Nullable BakedModel casingModel;

    public CasingPartRender(BlockState casing) {
        this.casing = casing;
    }

    @Override
    public DynamicRenderType<MultiblockControllerMachine, CasingPartRender> getType() {
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

    /** The controller's casing faces, under its overlays; plain ones without a level (items). */
    @OnlyIn(Dist.CLIENT)
    @Override
    public List<BakedQuad> getRenderQuads(@Nullable MultiblockControllerMachine machine,
                                          @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                          @Nullable BlockState blockState, @Nullable Direction side,
                                          RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        // machines render in the cutout layer; another pass (covers) mustn't draw the casing again
        if (renderType != null && renderType != RenderType.cutout()) return List.of();
        BakedModel model = casingModel();
        ModelData data = level != null && pos != null ? model.getModelData(level, pos, casing, modelData) :
                ModelData.EMPTY;
        return model.getQuads(casing, side, rand, data, renderType);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderPartModel(List<BakedQuad> quads, MultiblockControllerMachine controller,
                                MultiblockPartMachine part, Direction frontFacing, @Nullable Direction side,
                                RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        Level level = controller.getLevel();
        if (level == null) return;
        BakedModel model = casingModel();
        ModelData data = model.getModelData(level, part.getBlockPos(), casing, modelData);
        quads.addAll(model.getQuads(casing, side, rand, data, renderType));
    }

    private BakedModel casingModel() {
        if (casingModel == null) casingModel = RenderUtil.getModelForState(casing);
        return casingModel;
    }
}
