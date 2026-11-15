package com.yiran.minecraft.gtmqol.common.beacon;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

/** While its Personal Beacon is formed, the beacon keeps every chunk it covers loaded. */
public class PersonalBeaconChunkLoaderMachine extends MultiblockPartMachine {

    public PersonalBeaconChunkLoaderMachine(BlockEntityCreationInfo info) {
        super(info);
    }
}
