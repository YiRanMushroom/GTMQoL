package com.yiran.minecraft.gtmqol.integration.ae2.datacenter;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.joml.Matrix4f;

/**
 * A formed ME Data Center's glass as windows of stars, drawn like the End portal: one face over the glass of each
 * side, just outside it, so nothing behind the glass shows through. Only with AE2 loaded.
 */
// Untested: a formed ME Data Center's glass is covered by the End portal's stars on every side, at any size and facing;
// the CPU frames don't show through from far away; the frames have their overlay on all six sides
public class MEDataCenterStarsRender extends DynamicRender<MEDataCenterMachine, MEDataCenterStarsRender> {

    public static final Codec<MEDataCenterStarsRender> CODEC = Codec.unit(MEDataCenterStarsRender::new);
    public static final DynamicRenderType<MEDataCenterMachine, MEDataCenterStarsRender> TYPE =
            new DynamicRenderType<>(CODEC);

    /**
     * From the inside to a face: through the glass, one block, and past its outer surface by as little as GTCEu lifts
     * its overlays off a block (1/1024 per layer on 1.21, 0.01/16 on 1.20), which doesn't show.
     */
    private static final float OUT = 1 + 2 / 1024f;

    @Override
    public DynamicRenderType<MEDataCenterMachine, MEDataCenterStarsRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public boolean shouldRender(MEDataCenterMachine machine, Vec3 cameraPos) {
        return machine.isFormed() && machine.box() != 0 && super.shouldRender(machine, cameraPos);
    }

    @Override
    public boolean shouldRenderOffScreen(MEDataCenterMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(MEDataCenterMachine machine) {
        return new AABB(machine.getBlockPos()).inflate(15);
    }

    @Override
    public void render(MEDataCenterMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        int box = machine.box();
        if (!machine.isFormed() || box == 0) return;
        int up = box & 15;
        int left = box >> 4 & 15;
        int right = box >> 8 & 15;
        int back = box >> 12 & 15;
        Direction front = machine.getFrontFacing();
        Direction toLeft = front.getCounterClockWise();
        Direction toBack = front.getOpposite();
        // the two far corners of the inside, in blocks from the controller
        int ax = toLeft.getStepX() * (left - 1) + toBack.getStepX();
        int az = toLeft.getStepZ() * (left - 1) + toBack.getStepZ();
        int bx = -toLeft.getStepX() * (right - 1) + toBack.getStepX() * (back - 1);
        int bz = -toLeft.getStepZ() * (right - 1) + toBack.getStepZ() * (back - 1);
        // the inside, which is as large as the glass of each side
        float x0 = Math.min(ax, bx);
        float x1 = Math.max(ax, bx) + 1;
        float y0 = 1;
        float y1 = up;
        float z0 = Math.min(az, bz);
        float z1 = Math.max(az, bz) + 1;
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.endPortal());
        // as TheEndPortalRenderer draws its cube: south, north, east, west, down, up
        face(consumer, pose, x0, x1, y0, y1, z1 + OUT, z1 + OUT, z1 + OUT, z1 + OUT);
        face(consumer, pose, x0, x1, y1, y0, z0 - OUT, z0 - OUT, z0 - OUT, z0 - OUT);
        face(consumer, pose, x1 + OUT, x1 + OUT, y1, y0, z0, z1, z1, z0);
        face(consumer, pose, x0 - OUT, x0 - OUT, y0, y1, z0, z1, z1, z0);
        face(consumer, pose, x0, x1, y0 - OUT, y0 - OUT, z0, z0, z1, z1);
        face(consumer, pose, x0, x1, y1 + OUT, y1 + OUT, z1, z1, z0, z0);
    }

    private static void face(VertexConsumer consumer, Matrix4f pose, float x0, float x1, float y0, float y1,
                             float z0, float z1, float z2, float z3) {
        consumer.vertex(pose, x0, y0, z0).endVertex();
        consumer.vertex(pose, x1, y0, z1).endVertex();
        consumer.vertex(pose, x1, y1, z2).endVertex();
        consumer.vertex(pose, x0, y1, z3).endVertex();
    }
}
