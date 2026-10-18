package com.yiran.minecraft.gtmqol.common.crystal;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.yiran.minecraft.gtmqol.common.blockprovider.BlockProviderTrait;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/**
 * Provides the block in the middle of the structure, right behind the controller, to its recipes. That only works
 * without extended facing.
 */
public class CrystalGrowthMachine extends WorkableElectricMultiblockMachine {

    private final BlockProviderTrait centerBlock;

    public CrystalGrowthMachine(BlockEntityCreationInfo info) {
        super(info);
        this.centerBlock = attachTrait(new BlockProviderTrait(
                machine -> machine.getBlockPos().relative(machine.getFrontFacing().getOpposite())));
    }

    @Override
    public void onNeighborChanged(Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.onNeighborChanged(neighborBlock, neighborPos, isMoving);
        if (neighborPos.equals(centerBlock.getPosition())) centerBlock.notifyListeners();
    }
}
