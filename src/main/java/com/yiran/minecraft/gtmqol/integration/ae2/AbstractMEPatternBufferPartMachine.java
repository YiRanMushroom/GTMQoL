package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractMEPatternBufferPartMachine extends MEPatternBufferPartMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEPatternBufferPartMachine.class, MEBusPartMachine.MANAGED_FIELD_HOLDER);

    public int getMaxPatternCount() {
        IntIntImmutablePair gridSize = getPatternGridSize();
        return gridSize.leftInt() * gridSize.rightInt();
    }

    public abstract IntIntImmutablePair getPatternGridSize();

    public AbstractMEPatternBufferPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public NotifiableFluidTank createSharedFluidTank() {
        return new NotifiableFluidTank(this, 25, Integer.MAX_VALUE, IO.IN, IO.NONE);
    }

    public NotifiableItemStackHandler createSharedItemStackHandler() {
        return new NotifiableItemStackHandler(this, 25, IO.IN, IO.NONE);
    }
}