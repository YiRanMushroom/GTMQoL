package com.yiran.minecraft.gtmqol.common.circuit;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Makes GT circuits and Mekanism control circuits interchangeable, tier by tier: {@code c:circuits/basic}
 * includes {@code #gtceu:circuits/lv}, and {@code gtceu:circuits/lv} includes Mekanism's basic control circuit.
 * Including each tag in the other would be a cycle, which the tag loader rejects, hence one tag reference plus one
 * item.
 * <p>
 * ULV pairs with infused alloy the same way, through {@code c:alloys/advanced} (Mekanism's
 * {@code mekanism:alloys/infused} includes that tag).
 * <p>
 * Added while tags load ({@code MixinHelpersMixin}), only with Mekanism installed and the option on: what concerns
 * another mod than GTCEu or AE2 is never generated into the jar.
 */
// Untested: with Mekanism, GT recipes take Mekanism's control circuits of the tier and Mekanism recipes take GT
// circuits, as before; with circuits.mekanismCircuitTags off they no longer do after /reload
public final class CircuitTags {

    private record Pair(TagKey<Item> gt, String commonTag, String mekanismItem) {}

    private static final Pair[] PAIRS = {
            new Pair(CustomTags.ULV_CIRCUITS, "alloys/advanced", "alloy_infused"),
            new Pair(CustomTags.LV_CIRCUITS, "circuits/basic", "basic_control_circuit"),
            new Pair(CustomTags.MV_CIRCUITS, "circuits/advanced", "advanced_control_circuit"),
            new Pair(CustomTags.HV_CIRCUITS, "circuits/elite", "elite_control_circuit"),
            new Pair(CustomTags.EV_CIRCUITS, "circuits/ultimate", "ultimate_control_circuit"),
    };

    private CircuitTags() {}

    public static void addTo(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap) {
        if (!ModList.get().isLoaded("mekanism") || !GTMQoLConfig.get().circuits.mekanismCircuitTags) return;
        for (Pair pair : PAIRS) {
            add(tagMap, ResourceLocation.fromNamespaceAndPath("c", pair.commonTag),
                    TagEntry.optionalTag(pair.gt.location()));
            add(tagMap, pair.gt.location(),
                    TagEntry.optionalElement(ResourceLocation.fromNamespaceAndPath("mekanism", pair.mekanismItem)));
        }
    }

    private static void add(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap, ResourceLocation tag,
                            TagEntry entry) {
        tagMap.computeIfAbsent(tag, path -> new ArrayList<>()).add(new TagLoader.EntryWithSource(entry, GTMQoL.MOD_ID));
    }
}
