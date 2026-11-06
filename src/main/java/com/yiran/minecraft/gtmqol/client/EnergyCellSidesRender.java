package com.yiran.minecraft.gtmqol.client;

import com.gregtechceu.gtceu.client.model.quad.StaticFaceBakery;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyMachines;
import com.yiran.minecraft.gtmqol.integration.ae2.WirelessEnergyCellMachine;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** The wireless overlay on the sides of a wireless energy cell without an ME connection. Only with AE2 loaded. */
public class EnergyCellSidesRender extends DynamicRender<WirelessEnergyCellMachine, EnergyCellSidesRender> {

    public static final Codec<EnergyCellSidesRender> CODEC = Codec.unit(EnergyCellSidesRender::new);
    public static final DynamicRenderType<WirelessEnergyCellMachine, EnergyCellSidesRender> TYPE =
            new DynamicRenderType<>(CODEC);

    @Override
    public DynamicRenderType<WirelessEnergyCellMachine, EnergyCellSidesRender> getType() {
        return TYPE;
    }

    @Override
    public void render(WirelessEnergyCellMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    @Override
    public boolean shouldRender(WirelessEnergyCellMachine machine, Vec3 cameraPos) {
        return false;
    }

    @Override
    public boolean isBlockEntityRenderer() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<BakedQuad> getRenderQuads(@Nullable WirelessEnergyCellMachine machine,
                                          @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                          @Nullable BlockState blockState, @Nullable Direction side,
                                          RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        if (side == null || !EnergyCubeSidesRender.inLayer(machine != null, renderType)) return List.of();
        if (machine != null && machine.isConnected(side)) return List.of();
        return List.of(EnergyCubeSidesRender.quad(side, WirelessEnergyMachines.WIRELESS_OVERLAY,
                StaticFaceBakery.OUTPUT_OVERLAY));
    }
}
