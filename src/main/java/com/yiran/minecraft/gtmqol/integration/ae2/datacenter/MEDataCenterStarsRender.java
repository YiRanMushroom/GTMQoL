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
import com.mojang.serialization.MapCodec;
import org.joml.Matrix4f;

/**
 * The inside of a formed ME Data Center as a box of stars, drawn like the End portal. Its faces lie just inside the
 * glass, in front of the CPU frames. Only with AE2 loaded.
 */
public class MEDataCenterStarsRender extends DynamicRender<MEDataCenterMachine, MEDataCenterStarsRender> {

    public static final MapCodec<MEDataCenterStarsRender> CODEC = MapCodec.unit(MEDataCenterStarsRender::new);
    public static final DynamicRenderType<MEDataCenterMachine, MEDataCenterStarsRender> TYPE =
            new DynamicRenderType<>(CODEC);

    /** How far the faces reach into the glass around the inside. */
    private static final float INTO_GLASS = 0.01f;

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
        float x0 = Math.min(ax, bx) - INTO_GLASS;
        float x1 = Math.max(ax, bx) + 1 + INTO_GLASS;
        float y0 = 1 - INTO_GLASS;
        float y1 = up + INTO_GLASS;
        float z0 = Math.min(az, bz) - INTO_GLASS;
        float z1 = Math.max(az, bz) + 1 + INTO_GLASS;
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.endPortal());
        // as TheEndPortalRenderer draws its cube: south, north, east, west, down, up
        face(consumer, pose, x0, x1, y0, y1, z1, z1, z1, z1);
        face(consumer, pose, x0, x1, y1, y0, z0, z0, z0, z0);
        face(consumer, pose, x1, x1, y1, y0, z0, z1, z1, z0);
        face(consumer, pose, x0, x0, y0, y1, z0, z1, z1, z0);
        face(consumer, pose, x0, x1, y0, y0, z0, z0, z1, z1);
        face(consumer, pose, x0, x1, y1, y1, z1, z1, z0, z0);
    }

    private static void face(VertexConsumer consumer, Matrix4f pose, float x0, float x1, float y0, float y1,
                             float z0, float z1, float z2, float z3) {
        consumer.addVertex(pose, x0, y0, z0);
        consumer.addVertex(pose, x1, y0, z1);
        consumer.addVertex(pose, x1, y1, z2);
        consumer.addVertex(pose, x0, y1, z3);
    }
}
