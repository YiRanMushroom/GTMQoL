package com.yiran.minecraft.gtmqol.common.ui;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A slot that never holds anything: what it gets goes straight on to where the items are kept (the many inventories
 * behind an {@link ItemGridWidget}). So a window has one place to put items in, by hand or by Shift-click.
 */
public final class DepositHandler implements IItemHandlerModifiable {

    private final BooleanSupplier remote;
    private final Predicate<ItemStack> accepts;
    private final UnaryOperator<ItemStack> add;

    /**
     * @param remote  whether this is the client, which doesn't keep the items: it only checks {@code accepts}
     * @param accepts whether the item may go in; on the server also whether there is room
     * @param add     stores what it can of the stack and returns the rest; server only
     */
    public DepositHandler(BooleanSupplier remote, Predicate<ItemStack> accepts, UnaryOperator<ItemStack> add) {
        this.remote = remote;
        this.accepts = accepts;
        this.add = add;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (!remote.getAsBoolean() && !stack.isEmpty()) add.apply(stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) return stack;
        if (!simulate) return add.apply(stack);
        return stack.copyWithCount(stack.getCount() - 1);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return accepts.test(stack);
    }
}
