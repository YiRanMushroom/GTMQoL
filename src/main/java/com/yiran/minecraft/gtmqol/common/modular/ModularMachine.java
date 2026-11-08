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
        // generators keep their progress while they wait, as GTCEu's do
        getRecipeLogic().setRegressWhenWaiting(!isGenerator());
    }

    /** From its recipe types: GTCEu registers them after the machine definitions, so the definition can't say. */
    // Untested: modular generators (modular steam turbine) make EU and keep their progress while waiting
    @Override
    public boolean isGenerator() {
        return ModularMachines.isGenerator(getDefinition());
    }

    @Override
    public long getMaxVoltage() {
        return getOverclockVoltage();
    }
}
