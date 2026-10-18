package com.yiran.minecraft.gtmqol.common.ghost;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

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
    public ListTag serializeNBT() {
        ListTag tag = new ListTag();
        for (var ghost : list) {
            GhostIngredient.CODEC.encodeStart(NbtOps.INSTANCE, ghost).result().ifPresent(tag::add);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(ListTag tag) {
        list.clear();
        for (Tag entry : tag) {
            var result = GhostIngredient.CODEC.parse(NbtOps.INSTANCE, entry);
            result.result().ifPresentOrElse(list::add, () -> GTMQoL.LOGGER.warn("Dropping a ghost ingredient: {}",
                    result.error().map(error -> error.message()).orElse("")));
        }
    }
}
