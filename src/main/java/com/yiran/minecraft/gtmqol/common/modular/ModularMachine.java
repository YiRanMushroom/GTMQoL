package com.yiran.minecraft.gtmqol.common.modular;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

/**
 * Recipe tier follows the overclock voltage (which counts amperage), so a single multi-amp hatch can run
 * higher-tier recipes. Vanilla only allows tier + 1, and only with several hatches at the highest tier.
 */
public class ModularMachine extends WorkableElectricMultiblockMachine {

    public ModularMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public long getMaxVoltage() {
        return getOverclockVoltage();
    }
}
