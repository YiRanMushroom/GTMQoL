package com.yiran.minecraft.gtmqol.integration.productivebees;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Productive Bees ships gtceu recipes (the ore bees) written for gtceu 1.x, where capability keys were bare names like
 * {@code "item"} and {@code "eu"}. v8 reads them as registry ids, so they parse as {@code minecraft:item} and the
 * recipes fail to load. Remapping {@code minecraft:<path>} to each gtceu capability makes them load again.
 */
public final class LegacyCapabilityAliases {

    private LegacyCapabilityAliases() {}

    /** Call on the capability {@code RegisterEvent}, the only time the registry isn't frozen. */
    public static void register() {
        var registry = GTRegistries.RECIPE_CAPABILITIES;
        for (ResourceLocation id : List.copyOf(registry.keys())) {
            if (!id.getNamespace().equals("gtceu")) continue;
            ResourceLocation alias = ResourceLocation.withDefaultNamespace(id.getPath());
            if (!registry.containsKey(alias)) registry.remap(alias, id);
        }
    }
}
