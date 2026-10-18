package com.yiran.minecraft.gtmqol.common.singleblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;

/**
 * An electric single block with extra {@link SingleblockFeature}s, made with
 * {@link com.yiran.minecraft.gtmqol.GTMQoLAddon#tieredMachine}.
 */
public class ComposableTieredMachine extends SimpleTieredMachine {

    public ComposableTieredMachine(BlockEntityCreationInfo info, int tier, SingleblockFeature... features) {
        super(info, tier);
        for (SingleblockFeature feature : features) {
            feature.attach(this);
        }
    }
}
