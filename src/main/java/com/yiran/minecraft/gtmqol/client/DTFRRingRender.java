package com.yiran.minecraft.gtmqol.client;

import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.yiran.minecraft.gtmqol.common.multiblock.DTFRMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import org.lwjgl.opengl.GL11;

import static net.minecraft.util.FastColor.ARGB32.*;

/** gtceu's {@code FusionRingRender} without the bloom, in white. */
public class DTFRRingRender extends DynamicRender<DTFRMachine, DTFRRingRender> {

    public static final MapCodec<DTFRRingRender> CODEC = MapCodec.unit(DTFRRingRender::new);
    public static final DynamicRenderType<DTFRMachine, DTFRRingRender> TYPE = new DynamicRenderType<>(CODEC);

    private static final float FADEOUT = 60;
    private static final int COLOR = 0xFFFFFFFF;

    @Override
    public DynamicRenderType<DTFRMachine, DTFRRingRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(DTFRMachine machine, Vec3 cameraPos) {
        return (machine.recipeLogic.isWorking() || machine.delta > 0) && super.shouldRender(machine, cameraPos);
    }

    @Override
    public void render(DTFRMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.recipeLogic.isWorking() && machine.delta <= 0) {
            return;
        }
        renderLightRing(machine, partialTick, poseStack, buffer.getBuffer(GTRenderTypes.lightRing()));
    }

    private void renderLightRing(DTFRMachine machine, float partialTicks, PoseStack stack, VertexConsumer buffer) {
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        float alpha = 1f;
        if (machine.recipeLogic.isWorking()) {
            machine.lastColor = COLOR;
            machine.delta = FADEOUT;
        } else {
            alpha = machine.delta / FADEOUT;
            machine.lastColor = color(Mth.floor(alpha * 255), red(machine.lastColor), green(machine.lastColor),
                    blue(machine.lastColor));
            machine.delta -= Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
        }

        final var lerpFactor = Math.abs((Math.abs(machine.getOffsetTimer() % 50) + partialTicks) - 25) / 25;
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        var back = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        var axis = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped).getAxis();
        var r = Mth.lerp(lerpFactor, red(machine.lastColor), 255) / 255f;
        var g = Mth.lerp(lerpFactor, green(machine.lastColor), 255) / 255f;
        var b = Mth.lerp(lerpFactor, blue(machine.lastColor), 255) / 255f;
        RenderBufferHelper.renderRing(stack, buffer,
                back.getStepX() * 7 + 0.5F,
                back.getStepY() * 7 + 0.5F,
                back.getStepZ() * 7 + 0.5F,
                6, 0.2F, 10, 20,
                r, g, b, alpha, axis);

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    @Override
    public boolean shouldRenderOffScreen(DTFRMachine machine) {
        return machine.recipeLogic.isWorking() || machine.delta > 0;
    }

    @Override
    public AABB getRenderBoundingBox(DTFRMachine machine) {
        return new AABB(machine.getBlockPos()).inflate(getViewDistance() / 2.0D);
    }
}
