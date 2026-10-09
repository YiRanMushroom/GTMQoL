package com.yiran.minecraft.gtmqol.common.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Items a machine has produced but not yet put into its output buses. Unlike a recipe's outputs, nothing is voided:
 * whatever doesn't fit stays here until {@link #output()} is called again. Items keep their NBT (enchanted
 * books, damaged tools), identical ones are merged.
 */
public class PendingOutputTrait extends MachineTrait {

    /** One entry, for the UI. */
    public record Stored(ItemStack item, long count) {

        public static Stored read(FriendlyByteBuf buf) {
            return new Stored(buf.readItem(), buf.readVarLong());
        }

        public static void write(FriendlyByteBuf buf, Stored stored) {
            buf.writeItem(stored.item);
            buf.writeVarLong(stored.count);
        }
    }

    /** One of each item (count 1); {@link #counts} holds the amounts at the same index. */
    @SaveField
    private final List<ItemStack> items = new ArrayList<>();
    @SaveField
    private final List<Long> counts = new ArrayList<>();

    @Override
    protected List<Class<?>> validMachineClasses() {
        return List.of(MultiblockControllerMachine.class);
    }

    @Override
    public MultiblockControllerMachine getMachine() {
        return (MultiblockControllerMachine) super.getMachine();
    }

    public void add(ItemStack stack, long count) {
        if (stack.isEmpty() || count <= 0) return;
        for (int i = 0; i < items.size(); i++) {
            if (ItemStack.isSameItemSameTags(items.get(i), stack)) {
                counts.set(i, counts.get(i) + count);
                getMachine().markAsChanged();
                return;
            }
        }
        items.add(stack.copyWithCount(1));
        counts.add(count);
        getMachine().markAsChanged();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public long total() {
        long total = 0;
        for (long count : counts) total += count;
        return total;
    }

    /**
     * Moves as much as fits into the output buses (ME output buses included).
     *
     * @return whether everything was output
     */
    public boolean output() {
        if (items.isEmpty()) return true;
        List<NotifiableItemStackHandler> handlers = getMachine()
                .getTraitsFromParts(NotifiableItemStackHandler.class).stream()
                .filter(handler -> handler.getHandlerIO() == IO.OUT)
                .toList();
        for (int i = items.size() - 1; i >= 0; i--) {
            ItemStack item = items.get(i);
            long left = counts.get(i);
            int maxStack = item.getMaxStackSize();
            for (NotifiableItemStackHandler handler : handlers) {
                for (int slot = 0; slot < handler.getSlots() && left > 0; slot++) {
                    while (left > 0) {
                        int amount = (int) Math.min(left, maxStack);
                        int inserted = amount - handler.insertItemInternal(slot, item.copyWithCount(amount), false)
                                .getCount();
                        left -= inserted;
                        if (inserted < amount) break;
                    }
                }
            }
            if (left > 0) {
                counts.set(i, left);
            } else {
                items.remove(i);
                counts.remove(i);
            }
        }
        getMachine().markAsChanged();
        return items.isEmpty();
    }

    /** Largest first. */
    public List<Stored> storedItems() {
        List<Stored> stored = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) stored.add(new Stored(items.get(i), counts.get(i)));
        stored.sort(Comparator.comparingLong(Stored::count).reversed());
        return stored;
    }
}
