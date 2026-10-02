package com.yiran.minecraft.gtmqol.circuit;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * One universal circuit per tier, tagged as that tier's GT circuit. Made from any circuit of the tier in the
 * magical assembler, so the whole tier collapses into one item (handy for AE patterns).
 */
public final class UniversalCircuits {

    @SuppressWarnings("unchecked")
    public static final ItemEntry<Item>[] UNIVERSAL_CIRCUITS = new ItemEntry[TIER_COUNT];

    private UniversalCircuits() {}

    public static void init() {
        for (int tier : ALL_TIERS) {
            UNIVERSAL_CIRCUITS[tier] = GTMQoLAddon.registrate()
                    .item(VN[tier].toLowerCase(Locale.ROOT) + "_universal_circuit", Item::new)
                    .lang(VN[tier] + " Universal Circuit")
                    .tag(CustomTags.CIRCUITS_ARRAY[tier])
                    .register();
        }
    }
}
