package com.yiran.minecraft.gtmqol.api;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import java.util.List;

public class NotifiableCatalystFluidTank extends NotifiableFluidTank implements ISlotHint {

    public NotifiableCatalystFluidTank(MetaMachine machine, int slots, int capacity, IO io, IO capabilityIO) {
        super(machine, slots, capacity, io, capabilityIO);
    }

    public NotifiableCatalystFluidTank(MetaMachine machine, List<CustomFluidTank> storages, IO io, IO capabilityIO) {
        super(machine, storages, io, capabilityIO);
    }

    public NotifiableCatalystFluidTank(MetaMachine machine, int slots, int capacity, IO io) {
        super(machine, slots, capacity, io);
    }

    public NotifiableCatalystFluidTank(MetaMachine machine, List<CustomFluidTank> storages, IO io) {
        super(machine, storages, io);
    }

    public static ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableFluidTank.class, NotifiableFluidTank.MANAGED_FIELD_HOLDER);

    @Override
    public boolean qol$isCatalystSlot() {
        return true;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
