package com.yiran.minecraft.gtmqol.common.beacon;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;

/** A vanilla beacon beam from the top of a working Personal Beacon, in the colour of its effects. */
public class PersonalBeaconBeamRender extends DynamicRender<PersonalBeaconMachine, PersonalBeaconBeamRender> {

    public static final MapCodec<PersonalBeaconBeamRender> CODEC = MapCodec.unit(PersonalBeaconBeamRender::new);
    public static final DynamicRenderType<PersonalBeaconMachine, PersonalBeaconBeamRender> TYPE =
            new DynamicRenderType<>(CODEC);

    @Override
    public DynamicRenderType<PersonalBeaconMachine, PersonalBeaconBeamRender> getType() {
        return TYPE;
    }

    private static boolean shows(PersonalBeaconMachine machine) {
        return machine.isFormed() && machine.beamColor() != 0;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(PersonalBeaconMachine machine, Vec3 cameraPos) {
        return shows(machine) && super.shouldRender(machine, cameraPos);
    }

    @Override
    public boolean shouldRenderOffScreen(PersonalBeaconMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(PersonalBeaconMachine machine) {
        var pos = machine.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, Level.MAX_LEVEL_SIZE, pos.getZ() + 1);
    }

    @Override
    public void render(PersonalBeaconMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        Level level = machine.getLevel();
        if (level == null || !shows(machine)) return;
        // from inside the controller, as vanilla's, so it shows through the glass
        int height = level.getMaxBuildHeight() - machine.getBlockPos().getY();
        if (height <= 0) return;
        BeaconRenderer.renderBeaconBeam(poseStack, buffer, BeaconRenderer.BEAM_LOCATION, partialTick, 1,
                level.getGameTime(), 0, height, machine.beamColor(), 0.2f, 0.25f);
    }
}
