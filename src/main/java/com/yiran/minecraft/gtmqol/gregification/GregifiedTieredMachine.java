package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;

/**
 * Gregified single block: a {@link SimpleTieredMachine} with a batch mode toggle.
 */
public class GregifiedTieredMachine extends SimpleTieredMachine {

    public GregifiedTieredMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        attachPersistentTrait("batchMode", new BatchModeTrait());
    }
}
