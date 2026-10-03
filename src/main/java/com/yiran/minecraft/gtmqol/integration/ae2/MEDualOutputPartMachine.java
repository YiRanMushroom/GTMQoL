package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.storage.MEStorage;

import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * ME output bus and output hatch in one. Items go through the ME output bus's buffer, fluids through a second
 * buffer here. Both are pushed into the network as soon as something lands in them; whatever the network does not
 * take stays and is retried every {@code updateIntervals} ticks. Only the item buffer is shown in the UI.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEDualOutputPartMachine extends MEOutputBusPartMachine {

    @SaveField
    private KeyStorage fluidBuffer = new KeyStorage();

    // Set when the network refused something, so the retries are paced by shouldSyncME instead of every tick.
    private boolean blocked;

    public MEDualOutputPartMachine(BlockEntityCreationInfo info) {
        super(info);
        getInventory().addChangedListener(() -> blocked = false);
        var tank = attachTrait(new FluidBufferTank(fluidBuffer));
        fluidBuffer.setOnContentsChanged(() -> {
            tank.onContentsChanged();
            blocked = false;
            updateInventorySubscription();
        });
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        var grid = getMainNode().getGrid();
        if (grid != null && !fluidBuffer.isEmpty()) {
            for (var entry : fluidBuffer) {
                grid.getStorageService().getInventory().insert(entry.getKey(), entry.getLongValue(),
                        Actionable.MODULATE, actionSource);
            }
        }
    }

    @Override
    protected boolean shouldSubscribe() {
        return isWorkingEnabled() && isOnline() && (!fluidBuffer.isEmpty() || storageIterator().hasNext());
    }

    @Override
    public void autoIO() {
        if (blocked && !shouldSyncME()) return;
        if (!updateMEStatus()) return;
        var grid = getMainNode().getGrid();
        if (grid != null) {
            var network = grid.getStorageService().getInventory();
            if (!fluidBuffer.isEmpty()) fluidBuffer.insertInventory(network, actionSource);
            flushItems(network);
            blocked = !fluidBuffer.isEmpty() || storageIterator().hasNext();
        }
        updateInventorySubscription();
    }

    // The superclass's item buffer is private and its own autoIO only runs every updateIntervals ticks, so go
    // through storageIterator() (live entries of the same buffer) instead.
    private void flushItems(MEStorage network) {
        var it = storageIterator();
        boolean changed = false;
        while (it.hasNext()) {
            var entry = it.next();
            long amount = entry.getLongValue();
            long inserted = network.insert(entry.getKey(), amount, Actionable.MODULATE, actionSource);
            if (inserted <= 0) continue;
            changed = true;
            if (inserted >= amount) it.remove();
            else entry.setValue(amount - inserted);
        }
        if (changed) getInventory().notifyListeners();
    }

    private static class FluidBufferTank extends NotifiableFluidTank {

        private final BufferStorage storage;

        FluidBufferTank(KeyStorage buffer) {
            super(List.of(new BufferStorage(buffer)), IO.OUT, IO.NONE);
            storage = (BufferStorage) getStorages()[0];
            allowSameFluids = true;
        }

        @Override
        public int getTanks() {
            return 128;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public void setFluidInTank(int tank, FluidStack fluidStack) {}

        @Override
        public int getTankCapacity(int tank) {
            return storage.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public List<SizedFluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedFluidIngredient> left,
                                                            boolean simulate) {
            if (io != IO.OUT) return left;
            FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
            for (var it = left.listIterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.ingredient().hasNoFluids()) {
                    it.remove();
                    continue;
                }
                FluidStack[] fluids;
                if (ingredient.ingredient() instanceof IntProviderFluidIngredient provider && simulate) {
                    fluids = new FluidStack[] { provider.getMaxSizeStack() };
                } else {
                    fluids = ingredient.getFluids();
                }
                if (fluids.length == 0 || fluids[0].isEmpty()) {
                    it.remove();
                    continue;
                }
                int remaining = ingredient.amount() - storage.fill(fluids[0], action);
                if (remaining > 0) it.set(new SizedFluidIngredient(ingredient.ingredient(), remaining));
                else it.remove();
            }
            return left;
        }
    }

    private static class BufferStorage extends CustomFluidTank {

        private final KeyStorage buffer;

        BufferStorage(KeyStorage buffer) {
            super(0);
            this.buffer = buffer;
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setFluid(FluidStack fluid) {}

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            var key = AEFluidKey.of(resource);
            int oldValue = GTMath.saturatedCast(buffer.storage.getOrDefault(key, 0));
            int change = Math.min(Integer.MAX_VALUE - oldValue, resource.getAmount());
            if (change > 0 && action.execute()) {
                buffer.storage.put(key, oldValue + change);
                buffer.onChanged();
            }
            return change;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }

        @Override
        public boolean supportsDrain(int tank) {
            return false;
        }
    }
}
