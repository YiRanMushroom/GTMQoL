package com.yiran.minecraft.gtmqol.integration.productivebees;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.List;

/**
 * Productive Bees ships gtceu recipes (the ore bees) written for gtceu 1.x, where capability keys were bare names like
 * {@code "item"} and {@code "eu"}. v8 reads them as registry ids, so they parse as {@code minecraft:item} and the
 * recipes fail to load. Aliasing {@code minecraft:<path>} to each gtceu capability makes them load again.
 */
public final class LegacyCapabilityAliases {

    private LegacyCapabilityAliases() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(LegacyCapabilityAliases::register));
    }

    private static void register() {
        var registry = GTRegistries.RECIPE_CAPABILITIES;
        for (ResourceLocation id : List.copyOf(registry.keySet())) {
            if (!id.getNamespace().equals("gtceu")) continue;
            ResourceLocation alias = ResourceLocation.withDefaultNamespace(id.getPath());
            if (!registry.containsKey(alias)) registry.addAlias(alias, id);
        }
    }
}
