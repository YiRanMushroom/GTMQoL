package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.yiran.minecraft.gtmqol.data.tag.MaterialAliasTags;
import com.yiran.minecraft.gtmqol.data.tag.WireTags;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The GT material tags an item has ({@code c:dusts/copper}, {@code c:wires/copper}...), while recipes convert, from
 * {@link LoadingTags}.
 */
public final class GTMaterialTags {

    private static @Nullable Map<Item, List<TagKey<Item>>> byItem;

    private GTMaterialTags() {}

    public static void init() {
        LoadingTags.onChange(GTMaterialTags::clear);
    }

    private static synchronized void clear() {
        byItem = null;
    }

    public static synchronized List<TagKey<Item>> of(Item item) {
        if (byItem == null) byItem = index(LoadingTags.all(BuiltInRegistries.ITEM));
        return byItem.getOrDefault(item, List.of());
    }

    private static Map<Item, List<TagKey<Item>>> index(Map<ResourceLocation, Collection<Holder<Item>>> tags) {
        Set<ResourceLocation> materialTags = new HashSet<>();
        ItemMaterialData.MATERIAL_ENTRY_ITEM_MAP.keySet().forEach(entry -> {
            if (entry.material() == null) return;
            entry.tagPrefix().getItemTags(entry.material()).forEach(tag -> materialTags.add(tag.location()));
            if (entry.tagPrefix() == TagPrefix.wireGtSingle.value()) {
                materialTags.add(WireTags.tag(entry.material().getName()));
            }
        });
        // other mods' spellings, which include GT's tags
        new ArrayList<>(materialTags).forEach(location -> materialTags.add(MaterialAliasTags.alias(location)));
        Map<Item, List<TagKey<Item>>> result = new HashMap<>();
        for (ResourceLocation location : materialTags) {
            Collection<Holder<Item>> holders = tags.get(location);
            if (holders == null) continue;
            TagKey<Item> tag = TagKey.create(Registries.ITEM, location);
            for (Holder<Item> holder : holders) {
                result.computeIfAbsent(holder.value(), item -> new ArrayList<>()).add(tag);
            }
        }
        return result;
    }
}
