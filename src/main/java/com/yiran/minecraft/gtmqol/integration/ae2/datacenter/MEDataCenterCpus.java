package com.yiran.minecraft.gtmqol.integration.ae2.datacenter;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The crafting CPUs of an {@link MEDataCenterMachine}: one per running job, holding the storage that job asked for,
 * and one more that offers what is left. A job submitted to that one makes it a job's CPU and a new one takes its
 * place. Every CPU has all the co-processors. The ME network learns of them through {@code CraftingServiceMixin}.
 */
final class MEDataCenterCpus implements INBTSerializable<CompoundTag> {

    private final List<MEDataCenterCore> jobs = new ArrayList<>();
    private @Nullable MEDataCenterCore free;
    /** Saved jobs, until {@link #start} makes their CPUs: that needs the level. */
    private final List<CompoundTag> saved = new ArrayList<>();
    private @Nullable MEDataCenterMachine dataCenter;
    private long storage;
    private int coProcessors;
    /** The grid that was last told about the CPUs. */
    private @Nullable IGrid grid;
    private boolean changed;

    /** Once the data center is in the world; never with an AE2 that {@link MEDataCenterCompat} rejects. */
    void start(MEDataCenterMachine dataCenter) {
        this.dataCenter = dataCenter;
        for (CompoundTag tag : saved) {
            var core = new MEDataCenterCore(dataCenter, tag.getCompound("cpu"));
            core.bytes = tag.getLong("bytes");
            jobs.add(core);
        }
        saved.clear();
        free = new MEDataCenterCore(dataCenter, null);
        apply();
    }

    /** The data center leaves the world: its jobs wait in {@link #saved}. */
    void stop(HolderLookup.Provider registries) {
        if (dataCenter == null) return;
        for (MEDataCenterCore core : jobs) saved.add(save(core, registries));
        jobs.clear();
        free = null;
        dataCenter = null;
        notifyGrid(null);
    }

    /** What the crafting units give; 0 storage while the structure isn't formed. */
    void setCapacity(long storage, int coProcessors) {
        this.storage = storage;
        this.coProcessors = coProcessors;
        apply();
    }

    private void apply() {
        if (free == null) return;
        for (MEDataCenterCore core : jobs) {
            core.cpu.storage = core.bytes;
            core.cpu.accelerator = coProcessors;
        }
        free.cpu.storage = Math.max(0, storage - used());
        free.cpu.accelerator = coProcessors;
        changed = true;
    }

    /** Storage the running jobs hold. */
    long used() {
        long used = 0;
        for (MEDataCenterCore core : jobs) used = saturatedAdd(used, core.bytes);
        return used;
    }

    long free() {
        return Math.max(0, storage - used());
    }

    int jobCount() {
        return jobs.size() + saved.size();
    }

    /** A job was submitted to this cluster; ours only if it is the one offering what is left. */
    void submitted(CraftingCPUCluster cpu, long bytes) {
        if (free == null || free.cpu != cpu || dataCenter == null) return;
        free.bytes = bytes;
        jobs.add(free);
        free = new MEDataCenterCore(dataCenter, null);
        apply();
        dataCenter.markAsChanged();
    }

    /** Gives back the storage of finished jobs, and keeps the ME network's list of CPUs current. */
    void tick(@Nullable IGrid now) {
        // a finished job's CPU stays until the network took what it held
        if (jobs.removeIf(core -> !core.cpu.isBusy() && core.cpu.craftingLogic.getInventory().list.isEmpty())) {
            apply();
            if (dataCenter != null) dataCenter.markAsChanged();
        }
        if (now != grid || changed) notifyGrid(now);
    }

    private void notifyGrid(@Nullable IGrid now) {
        changed = false;
        if (grid != null && grid != now) post(grid);
        grid = now;
        if (now != null) post(now);
    }

    /** Makes the grid's crafting service list its CPUs again. */
    private static void post(IGrid grid) {
        IGridNode node = grid.getPivot();
        if (node != null) grid.postEvent(new GridCraftingCpuChange(node));
    }

    /** The CPUs an ME network sees: every job's, and the free one while there is storage left. */
    List<CraftingCPUCluster> offered() {
        List<CraftingCPUCluster> cpus = new ArrayList<>(jobs.size() + 1);
        for (MEDataCenterCore core : jobs) cpus.add(core.cpu);
        if (free != null && free.cpu.getAvailableStorage() > 0) cpus.add(free.cpu);
        return cpus;
    }

    /** Ends every job; what they hold goes to the ME network, and what it doesn't take is gone. */
    void cancelAll() {
        for (MEDataCenterCore core : jobs) {
            core.cpu.cancelJob();
            // a job that had ended before but could not store everything
            core.cpu.craftingLogic.storeItems();
            core.cpu.craftingLogic.getInventory().clear();
        }
        jobs.clear();
        saved.clear();
        apply();
    }

    private static CompoundTag save(MEDataCenterCore core, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        CompoundTag cpu = new CompoundTag();
        core.cpu.writeToNBT(cpu, registries);
        tag.put("cpu", cpu);
        tag.putLong("bytes", core.bytes);
        return tag;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (MEDataCenterCore core : jobs) list.add(save(core, registries));
        list.addAll(saved);
        CompoundTag tag = new CompoundTag();
        tag.put("jobs", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        // only read before start: a running data center is what is saved
        if (dataCenter != null) return;
        saved.clear();
        ListTag list = tag.getList("jobs", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) saved.add(list.getCompound(i));
    }

    static long saturatedAdd(long a, long b) {
        long sum = a + b;
        return sum < 0 ? Long.MAX_VALUE : sum;
    }
}
