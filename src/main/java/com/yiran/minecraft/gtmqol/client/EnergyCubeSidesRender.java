package com.yiran.minecraft.gtmqol.client;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.client.model.quad.StaticFaceBakery;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.core.util.extensions.BakedQuadExt;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyCubeMachine;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyCubeMachine.SideMode;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyMachines;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
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
 * The wireless overlay on every side of an energy cube, and over it GT's FE converter overlay on the sides set to input
 * or output. Baked for each rebuild of the block like gtceu's auto output overlays
 * ({@code MachineModel#getMachineQuads}): six sides of three modes are too many for model properties.
 */
public class EnergyCubeSidesRender extends DynamicRender<WirelessEnergyCubeMachine, EnergyCubeSidesRender> {

    public static final MapCodec<EnergyCubeSidesRender> CODEC = MapCodec.unit(EnergyCubeSidesRender::new);
    public static final DynamicRenderType<WirelessEnergyCubeMachine, EnergyCubeSidesRender> TYPE =
            new DynamicRenderType<>(CODEC);

    @Override
    public DynamicRenderType<WirelessEnergyCubeMachine, EnergyCubeSidesRender> getType() {
        return TYPE;
    }

    @Override
    public void render(WirelessEnergyCubeMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    @Override
    public boolean shouldRender(WirelessEnergyCubeMachine machine, Vec3 cameraPos) {
        return false;
    }

    @Override
    public boolean isBlockEntityRenderer() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<BakedQuad> getRenderQuads(@Nullable WirelessEnergyCubeMachine machine,
                                          @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                          @Nullable BlockState blockState, @Nullable Direction side,
                                          RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        if (side == null || !inLayer(machine != null, renderType)) return List.of();
        // items have no machine, they show a new cube's sides
        SideMode mode = machine != null ? machine.getMode(side) : SideMode.NONE;
        BakedQuad wireless = quad(side, WirelessEnergyMachines.WIRELESS_OVERLAY, FaceLayer.MACHINE_FACE);
        if (mode == SideMode.NONE) return List.of(wireless);
        ResourceLocation texture = mode == SideMode.INPUT ? GTMachineModels.CONVERTER_FE_IN :
                GTMachineModels.CONVERTER_FE_OUT;
        return List.of(wireless, quad(side, texture, FaceLayer.MACHINE_DETAIL));
    }

    /** Machines render in the cutout layer, items in whatever they are given. */
    static boolean inLayer(boolean placed, @Nullable RenderType renderType) {
        return !placed || renderType == null || renderType == RenderType.cutout();
    }

    @OnlyIn(Dist.CLIENT)
    static BakedQuad quad(Direction side, ResourceLocation texture, FaceLayer layer) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        BakedQuad quad = StaticFaceBakery.bakeFace(StaticFaceBakery.OUTPUT_OVERLAY, side, sprite);
        // a mixin of gtceu's; without the layer the quad would sit above the covers
        return ((BakedQuadExt) quad).gtceu$setFaceLayer(layer);
    }
}
