package com.yiran.minecraft.gtmqol.api;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

public class NotifiableItemStackCatalystHandler extends NotifiableItemStackHandler implements ISlotHint {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableItemStackHandler.class, NotifiableItemStackHandler.MANAGED_FIELD_HOLDER);

    public NotifiableItemStackCatalystHandler(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO, IntFunction<CustomItemStackHandler> storageFactory) {
        super(machine, slots, handlerIO, capabilityIO, storageFactory);
    }

    public NotifiableItemStackCatalystHandler(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO) {
        super(machine, slots, handlerIO, capabilityIO);
    }

    public NotifiableItemStackCatalystHandler(MetaMachine machine, int slots, @NotNull IO handlerIO) {
        super(machine, slots, handlerIO);
    }

    @Override
    public boolean qol$isCatalystSlot() {
        return true;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
