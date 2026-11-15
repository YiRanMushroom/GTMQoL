package com.yiran.minecraft.gtmqol.integration.ae2.datacenter;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.BlockPos;

import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.me.cluster.implementations.CraftingCPUCluster;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Whether the installed AE2 still has the crafting CPU internals the ME Data Center builds on. They are reached through
 * our access transformer, which does nothing when a name is gone, so this touches each of them once, by reference and
 * not by name: a missing one fails to link here, and the data center then says so instead of crashing later.
 */
final class MEDataCenterCompat {

    private static Boolean compatible;

    private MEDataCenterCompat() {}

    static boolean isCompatible() {
        if (compatible == null) {
            try {
                Probe.run();
                compatible = true;
            } catch (LinkageError e) {
                GTMQoL.LOGGER.error("ME Data Center: this AE2 version is not supported", e);
                compatible = false;
            }
        }
        return compatible;
    }

    /** Its own class: one that fails to link fails here, inside the try. */
    private static final class Probe {

        @SuppressWarnings("unused")
        static void run() {
            // Untested: with an AE2 that lacks one of these, the first use throws a LinkageError here (method references
            // resolve when created) and an ME Data Center shows "This AE2 version is not supported"
            var cluster = new CraftingCPUCluster(BlockPos.ZERO, BlockPos.ZERO);
            cluster.storage = 0;
            cluster.accelerator = 0;
            BiConsumer<CraftingCPUCluster, CraftingBlockEntity> add = CraftingCPUCluster::addBlockEntity;
            Consumer<CraftingCPUCluster> done = CraftingCPUCluster::done;
            BiConsumer<CraftingBlockEntity, CraftingCPUCluster> status = CraftingBlockEntity::updateStatus;
        }
    }
}
