package com.yiran.minecraft.gtmqol;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.Map;

/**
 * Blocks and items that got a new ID. Worlds saved with an old ID load it as the new one: the block, its machine with
 * everything in it, and item stacks (patterns, the pattern batches of running crafting jobs). Old IDs stay here for good.
 */
public final class RenamedIds {

    /** Old path to new path, both in this mod's namespace. */
    private static final Map<String, String> RENAMED = Map.of(
            "pattern_matrix", "assembling_matrix",
            "matrix_me_interface", "assembling_matrix_interface",
            "matrix_pattern_core", "assembling_matrix_pattern_core",
            "matrix_processing_core", "assembling_matrix_processing_core",
            "scaled_crafting_pattern", "pattern_batch");

    private RenamedIds() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(RenamedIds::remap);
    }

    private static void remap(MissingMappingsEvent event) {
        remapIn(event, Registries.BLOCK, ForgeRegistries.BLOCKS);
        remapIn(event, Registries.ITEM, ForgeRegistries.ITEMS);
        remapIn(event, Registries.BLOCK_ENTITY_TYPE, ForgeRegistries.BLOCK_ENTITY_TYPES);
    }

    /** Only where the new ID exists: a feature switched off in the config registers nothing. */
    private static <T> void remapIn(MissingMappingsEvent event, ResourceKey<? extends Registry<T>> key,
                                    IForgeRegistry<T> registry) {
        for (var mapping : event.getMappings(key, GTMQoL.MOD_ID)) {
            String to = RENAMED.get(mapping.getKey().getPath());
            if (to == null) continue;
            ResourceLocation target = GTMQoL.id(to);
            // blocks and items fall back to air for unknown IDs, so check first
            if (registry.containsKey(target)) mapping.remap(registry.getValue(target));
        }
    }
}
