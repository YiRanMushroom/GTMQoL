package com.yiran.minecraft.gtmqol;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

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

    public static void init(IEventBus modBus) {
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(RenamedIds::register));
    }

    private static void register() {
        RENAMED.forEach((from, to) -> {
            alias(BuiltInRegistries.BLOCK, from, to);
            alias(BuiltInRegistries.ITEM, from, to);
            alias(BuiltInRegistries.BLOCK_ENTITY_TYPE, from, to);
        });
    }

    /** Only where the new ID exists: a feature switched off in the config registers nothing. */
    private static void alias(Registry<?> registry, String from, String to) {
        ResourceLocation target = GTMQoL.id(to);
        if (registry.containsKey(target)) registry.addAlias(GTMQoL.id(from), target);
    }
}
