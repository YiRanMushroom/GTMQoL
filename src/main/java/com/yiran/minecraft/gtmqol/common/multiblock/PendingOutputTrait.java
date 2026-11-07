package com.yiran.minecraft.gtmqol.common.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Items a machine has produced but not yet put into its output buses. Unlike a recipe's outputs, nothing is voided:
 * whatever doesn't fit stays here until {@link #output()} is called again. Items keep their components (enchanted
 * books, damaged tools), identical ones are merged.
 */
public class PendingOutputTrait extends MachineTrait {

    /** One entry, for the UI. */
    public record Stored(ItemStack item, long count) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Stored> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, Stored::item,
                ByteBufCodecs.VAR_LONG, Stored::count,
                Stored::new);

        /** By content: ItemStack has no equals of its own, and the UI resends the list whenever an entry differs. */
        @Override
        public boolean equals(Object other) {
            return other instanceof Stored stored && stored.count == count && ItemStack.matches(stored.item, item);
        }

        @Override
        public int hashCode() {
            return Long.hashCode(count) * 31 + item.getItem().hashCode();
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
            if (ItemStack.isSameItemSameComponents(items.get(i), stack)) {
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
    // Untested: a treasure fishing pond with thousands of pending unstackables on an ME output bus no longer lags
    public boolean output() {
        if (items.isEmpty()) return true;
        List<NotifiableItemStackHandler> handlers = getMachine()
                .getTraitsFromParts(NotifiableItemStackHandler.class).stream()
                .filter(handler -> handler.getHandlerIO() == IO.OUT)
                .toList();
        for (int i = items.size() - 1; i >= 0; i--) {
            ItemStack item = items.get(i);
            long left = counts.get(i);
            for (NotifiableItemStackHandler handler : handlers) {
                // The whole rest per slot: a bus slot takes what fits (slot limit, stack size) and returns the
                // rest, an ME output bus takes all of it. Stack-size pieces made thousands of unstackables (treasure)
                // thousands of inserts, each an AE key and a change notification on ME buses.
                for (int slot = 0; slot < handler.getSlots() && left > 0; slot++) {
                    int amount = (int) Math.min(left, Integer.MAX_VALUE);
                    left -= amount - handler.insertItemInternal(slot, item.copyWithCount(amount), false).getCount();
                }
                if (left <= 0) break;
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
