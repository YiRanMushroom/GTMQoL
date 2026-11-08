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
    private static final Map<String, String> RENAMED = Map.ofEntries(
            Map.entry("pattern_matrix", "assembling_matrix"),
            Map.entry("matrix_me_interface", "assembling_matrix_interface"),
            Map.entry("matrix_pattern_core", "assembling_matrix_pattern_core"),
            Map.entry("matrix_processing_core", "assembling_matrix_processing_core"),
            Map.entry("scaled_crafting_pattern", "pattern_batch"),
            Map.entry("fishing_pond", "industrial_fishing_pond"),
            Map.entry("wireless_computation_transmitter_hatch", "wireless_computation_transmission_hatch"),
            Map.entry("wireless_computation_receiver_hatch", "wireless_computation_reception_hatch"),
            Map.entry("wireless_data_transmitter_hatch", "wireless_optical_data_transmission_hatch"),
            Map.entry("wireless_data_receiver_hatch", "wireless_optical_data_reception_hatch"));

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
