package com.yiran.minecraft.gtmqol.integration.ae2.datacenter;

import appeng.api.networking.IGrid;
import appeng.crafting.CraftingLink;
import appeng.me.cluster.implementations.CraftingCPUCluster;

import java.util.Set;
import java.util.function.Consumer;

/** Where AE2's crafting service meets the ME Data Centers; called from the mixins. */
public final class MEDataCenterHooks {

    private MEDataCenterHooks() {}

    /**
     * After the crafting service listed the CPUs built of blocks: adds the CPUs of the grid's data centers, and their
     * jobs' links, as it does for its own.
     */
    public static void addCpus(IGrid grid, Set<CraftingCPUCluster> cpus, Consumer<CraftingLink> links) {
        for (var meInterface : grid.getMachines(MEDataCenterInterfaceMachine.class)) {
            MEDataCenterMachine dataCenter = meInterface.dataCenter();
            if (dataCenter == null) continue;
            for (CraftingCPUCluster cpu : dataCenter.cpus().offered()) {
                cpus.add(cpu);
                if (cpu.craftingLogic.getLastLink() instanceof CraftingLink link) links.accept(link);
            }
        }
    }

    /** A CPU took a job that needs {@code bytes} of storage. */
    public static void submitted(CraftingCPUCluster cpu, long bytes) {
        var blocks = cpu.getBlockEntities();
        if (blocks.hasNext() && blocks.next() instanceof MEDataCenterCore core) core.submitted(bytes);
    }
}
