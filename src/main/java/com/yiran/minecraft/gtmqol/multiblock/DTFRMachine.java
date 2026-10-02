package com.yiran.minecraft.gtmqol.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

/**
 * Plain workable multiblock; not a {@code FusionReactorMachine} so it has no internal energy buffer, start-up
 * heat or tier cap. The fields only hold the ring's fade-out state for {@code DTFRRingRender}, which is shared by
 * every DTFR.
 */
public class DTFRMachine extends WorkableElectricMultiblockMachine {

    public float delta;
    public int lastColor = -1;

    public DTFRMachine(BlockEntityCreationInfo info) {
        super(info);
    }
}
