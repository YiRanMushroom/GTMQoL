package com.yiran.minecraft.gtmqol.common.ui;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * An item in an {@link ItemGridWidget}: where it is (which inventory, which slot), the item, what stands for it in
 * the grid, and how many it stands for (shown when more than one). What inventory and slot mean is the owner's.
 */
public record ItemGridEntry(int inventory, int slot, ItemStack stack, ItemStack icon, int count) {

    /** A single item. */
    public ItemGridEntry(int inventory, int slot, ItemStack stack, ItemStack icon) {
        this(inventory, slot, stack, icon, 1);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemGridEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ItemGridEntry::inventory,
            ByteBufCodecs.VAR_INT, ItemGridEntry::slot,
            ItemStack.STREAM_CODEC, ItemGridEntry::stack,
            ItemStack.STREAM_CODEC, ItemGridEntry::icon,
            ByteBufCodecs.VAR_INT, ItemGridEntry::count,
            ItemGridEntry::new);

    /**
     * By content: ItemStack has no equals of its own, and the UI resends the list whenever an entry differs. The icon
     * follows from the item. Unchanged slots keep their entry, so most comparisons end at the first check.
     */
    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ItemGridEntry entry && entry.inventory == inventory &&
                entry.slot == slot && entry.count == count && ItemStack.matches(entry.stack, stack);
    }

    @Override
    public int hashCode() {
        return (31 * inventory + slot) * 31 + stack.getItem().hashCode();
    }
}
