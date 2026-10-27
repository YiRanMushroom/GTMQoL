package com.yiran.minecraft.gtmqol.data.tag;

import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.utils.TagUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GT's 1x wires under the {@code c:wires/<material>} tags other mods use for their wires (half an ingot each, like
 * GT's), added along with GT's runtime material tags ({@code MixinHelpersMixin}) so materials from KubeJS get them
 * too. One way only: GT's wire prefix doesn't know the tag, so GT recipes still take GT wires only.
 */
public final class WireTags {

    private WireTags() {}

    public static ResourceLocation tag(String material) {
        return TagUtil.createItemTag("wires/" + material).location();
    }

    public static void addTo(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap) {
        ItemMaterialData.MATERIAL_ENTRY_ITEM_MAP.forEach((entry, items) -> {
            if (entry.tagPrefix() != TagPrefix.wireGtSingle.value() || entry.material() == null || items.isEmpty()) return;
            var entries = items.stream().map(item -> MixinHelpers.makeItemEntry(item.get())).toList();
            String name = entry.material().getName();
            tagMap.computeIfAbsent(tag(name), path -> new ArrayList<>()).addAll(entries);
            String alias = MaterialAliasTags.ALIASES.get(name);
            if (alias != null) tagMap.computeIfAbsent(tag(alias), path -> new ArrayList<>()).addAll(entries);
        });
    }
}
