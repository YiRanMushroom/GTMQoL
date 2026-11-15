package com.yiran.minecraft.gtmqol.integration.ae2.datacenter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.jetbrains.annotations.Nullable;

/**
 * One crafting CPU of an {@link MEDataCenterMachine}: AE2's own {@link CraftingCPUCluster}, so jobs run, save and show
 * exactly as on a CPU built of blocks. A cluster asks its core block entity for everything about the world, and that
 * has to be a {@link CraftingBlockEntity}; this is one that is never placed. It answers with the data center: its
 * position and level, the grid node of its ME interface, and saving goes to the controller.
 */
final class MEDataCenterCore extends CraftingBlockEntity {

    private final MEDataCenterMachine dataCenter;
    final CraftingCPUCluster cpu;
    /** What its job reserved of the data center's storage; 0 for the CPU that offers what is left. */
    long bytes;

    /** @param saved what {@link CraftingCPUCluster#writeToNBT} wrote, to go on with that job */
    MEDataCenterCore(MEDataCenterMachine dataCenter, @Nullable CompoundTag saved) {
        // AE2's own type and block: the block entity constructor checks that they belong together
        super(AEBlockEntities.CRAFTING_UNIT, dataCenter.getBlockPos(),
                AEBlocks.CRAFTING_UNIT.block().defaultBlockState());
        this.dataCenter = dataCenter;
        setLevel(dataCenter.getLevel());
        // as CraftingCPUCalculator builds a cluster
        this.cpu = new CraftingCPUCluster(getBlockPos(), getBlockPos());
        updateStatus(cpu);
        cpu.addBlockEntity(this);
        if (saved != null) setPreviousState(saved);
        cpu.done();
    }

    void submitted(long bytes) {
        dataCenter.cpus().submitted(cpu, bytes);
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return dataCenter.cpuNode();
    }

    @Override
    public @Nullable IGridNode getGridNode() {
        return dataCenter.cpuNode();
    }

    @Override
    public boolean isActive() {
        IGridNode node = dataCenter.cpuNode();
        return node != null && node.isActive();
    }

    @Override
    public boolean isPowered() {
        return isActive();
    }

    @Override
    public void saveChanges() {
        dataCenter.markAsChanged();
    }

    /** The data center gives the cluster its storage and co-processors, not this block. */
    @Override
    public long getStorageBytes() {
        return 0;
    }

    @Override
    public int getAcceleratorThreads() {
        return 0;
    }

    @Override
    public AbstractCraftingUnitBlock<?> getUnitBlock() {
        return AEBlocks.CRAFTING_UNIT.block();
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return MEDataCenter.CONTROLLER.asStack().getItem();
    }

    /** What terminals call the CPU. */
    @Override
    public Component getCustomName() {
        return Component.translatable(MEDataCenter.CONTROLLER.getDescriptionId());
    }

    // there is no block in the world to update, break or drop items around

    @Override
    public void updateSubType(boolean updateFormed) {}

    @Override
    public void breakCluster() {}

    @Override
    public void disconnect(boolean update) {}

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {}
}
