package com.yiran.minecraft.gtmqol.common.ghost;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The marks of a {@link GhostIngredientHandler}, in order, no length limit. Saved through gtceu's
 * {@code @SaveField} (an {@link INBTSerializable} field). Entries that no longer load (their mod removed) are dropped.
 */
public final class GhostIngredientList implements INBTSerializable<ListTag> {

    private final List<GhostIngredient<?>> list = new ArrayList<>();
    private Runnable onChanged = () -> {};

    public void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public List<GhostIngredient<?>> view() {
        return Collections.unmodifiableList(list);
    }

    public int size() {
        return list.size();
    }

    public boolean contains(GhostIngredient<?> ghost) {
        return list.contains(ghost);
    }

    /** Sets entry {@code index}, or adds one at the end if {@code index} is the size. */
    public void set(int index, GhostIngredient<?> ghost) {
        if (index < 0 || index > list.size()) return;
        if (index == list.size()) list.add(ghost);
        else list.set(index, ghost);
        onChanged.run();
    }

    /** Removes entry {@code index}; the ones after it move up. */
    public void remove(int index) {
        if (index < 0 || index >= list.size()) return;
        list.remove(index);
        onChanged.run();
    }

    @Override
    public @UnknownNullability ListTag serializeNBT(HolderLookup.Provider provider) {
        ListTag tag = new ListTag();
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        for (var ghost : list) {
            GhostIngredient.CODEC.encodeStart(ops, ghost).ifSuccess(tag::add);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag tag) {
        list.clear();
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        for (Tag entry : tag) {
            GhostIngredient.CODEC.parse(ops, entry)
                    .ifSuccess(list::add)
                    .ifError(error -> GTMQoL.LOGGER.warn("Dropping a ghost ingredient: {}", error.message()));
        }
    }
}
