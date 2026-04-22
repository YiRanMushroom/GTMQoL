package com.yiran.minecraft.gtmqol.renderer;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType
import com.gregtechceu.gtceu.client.util.RenderBufferHelper
import com.lowdragmc.lowdraglib.utils.ColorUtils.*
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.serialization.Codec
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import kotlin.math.abs


class DTFRRingRenderer : DynamicRender<WorkableElectricMultiblockMachine, DTFRRingRenderer>() {
    companion object {
        val CODEC: Codec<DTFRRingRenderer> = Codec.unit(::DTFRRingRenderer)
        @JvmStatic
        val TYPE: DynamicRenderType<WorkableElectricMultiblockMachine, DTFRRingRenderer> =
            DynamicRenderType(DTFRRingRenderer.CODEC)
        val FADEOUT: Float = 60.0f
    }

    var delta: Float = 0f
    var lastColor: Int = -1

    override fun getType(): DynamicRenderType<WorkableElectricMultiblockMachine, DTFRRingRenderer> {
        return DTFRRingRenderer.TYPE
    }

    override fun shouldRender(machine: WorkableElectricMultiblockMachine, cameraPos: Vec3): Boolean {
        return machine.recipeLogic.isWorking || delta > 0;
    }

    override fun render(
        machine: WorkableElectricMultiblockMachine, partialTick: Float,
        poseStack: PoseStack, buffer: MultiBufferSource,
        packedLight: Int, packedOverlay: Int
    ) {
        if (!machine.recipeLogic.isWorking && delta <= 0) {
            return
        }
//        if (GTCEu.Mods.isShimmerLoaded()) {
//            val finalStack: PoseStack = RenderUtil.copyPoseStack(poseStack)
//            BloomUtils.entityBloom(Consumer { source: MultiBufferSource? ->
//                renderLightRing(
//                    machine, partialTick, finalStack,
//                    source!!.getBuffer(GTRenderTypes.getLightRing())
//                )
//            })
//        } else {
        renderLightRing(machine, partialTick, poseStack, buffer.getBuffer(GTRenderTypes.getLightRing()))
//        }
    }

    @OnlyIn(Dist.CLIENT)
    private fun renderLightRing(
        machine: WorkableElectricMultiblockMachine, partialTicks: Float, stack: PoseStack,
        buffer: VertexConsumer?
    ) {
        val color = color(255, 255, 255, 255)
        var alpha = 1f
        if (machine.recipeLogic.isWorking) {
            lastColor = color
            delta = FADEOUT
        } else {
            alpha = delta / FADEOUT
            lastColor = color(Mth.floor(alpha * 255).toFloat(), red(lastColor), green(lastColor), blue(lastColor))
            delta -= Minecraft.getInstance().deltaFrameTime
        }

        val lerpFactor = abs((abs(machine.offsetTimer % 50) + partialTicks) - 25) / 25
        val front = machine.frontFacing
        val upwards = machine.upwardsFacing
        val flipped = machine.isFlipped()
        val back =
            RelativeDirection.BACK.getRelative(front, upwards, flipped)
        val axis =
            RelativeDirection.UP.getRelative(front, upwards, flipped).axis
        val r =
            Mth.lerp(lerpFactor.toDouble(), red(lastColor).toDouble(), 255.toDouble()) / 255f
        val g =
            Mth.lerp(lerpFactor.toDouble(), green(lastColor).toDouble(), 255.toDouble()) / 255f
        val b =
            Mth.lerp(lerpFactor.toDouble(), blue(lastColor).toDouble(), 255.toDouble()) / 255f
        RenderBufferHelper.renderRing(
            stack, buffer,
            back.stepX * 7 + 0.5f,
            back.stepY * 7 + 0.5f,
            back.stepZ * 7 + 0.5f,
            6f, 0.2f, 10, 20,
            r.toFloat(), g.toFloat(), b.toFloat(), alpha, axis
        )
    }
}
