package com.yiran.minecraft.gtmqol.data.tag;

import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.utils.TagUtil;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import com.tterrag.registrate.providers.ProviderType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tags under the names other mods use for some GT materials: {@code c:ingots/aluminum} includes
 * {@code #c:ingots/aluminium}, {@code c:plutonium} (fluid) includes {@code #c:plutonium_239}, and so on for every
 * item, block and fluid tag of the material. GT's own material tags are generated at runtime, so they're referenced
 * as optional tags.
 */
public final class MaterialAliasTags {

    /** GT material name -> the name other mods use */
    public static final Map<String, String> ALIASES = Map.of(
            "aluminium", "aluminum",
            "plutonium_239", "plutonium");

    private MaterialAliasTags() {}

    public static void init() {
        GTMQoLAddon.registrate().addDataGenerator(ProviderType.ITEM_TAGS, prov -> {
            for (var location : aliasedTags(ItemMaterialData.MATERIAL_ENTRY_ITEM_MAP.keySet(), false)) {
                prov.addTag(TagKey.create(Registries.ITEM, alias(location)))
                        .addOptionalTag(location);
            }
        });
        GTMQoLAddon.registrate().addDataGenerator(ProviderType.BLOCK_TAGS, prov -> {
            for (var location : aliasedTags(ItemMaterialData.MATERIAL_ENTRY_BLOCK_MAP.keySet(), true)) {
                prov.addTag(TagKey.create(Registries.BLOCK, alias(location)))
                        .addOptionalTag(location);
            }
        });
        GTMQoLAddon.registrate().addDataGenerator(ProviderType.FLUID_TAGS, prov -> {
            for (String name : ALIASES.keySet()) {
                var location = TagUtil.createFluidTag(name).location();
                prov.addTag(TagKey.create(Registries.FLUID, alias(location)))
                        .addOptionalTag(location);
            }
        });
    }

    private static Set<ResourceLocation> aliasedTags(Set<MaterialEntry> entries, boolean blocks) {
        Set<ResourceLocation> result = new HashSet<>();
        for (MaterialEntry entry : entries) {
            Material material = entry.material();
            if (material == null || entry.tagPrefix() == null || !ALIASES.containsKey(material.getName())) continue;
            List<? extends TagKey<?>> tags = blocks ? entry.tagPrefix().getBlockTags(material) :
                    entry.tagPrefix().getItemTags(material);
            for (TagKey<?> tag : tags) {
                if (tag != null && tag.location().getPath().contains(material.getName())) result.add(tag.location());
            }
        }
        return result;
    }

    private static ResourceLocation alias(ResourceLocation location) {
        String path = location.getPath();
        for (var alias : ALIASES.entrySet()) path = path.replace(alias.getKey(), alias.getValue());
        return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), path);
    }
}
