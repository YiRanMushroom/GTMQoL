package com.yiran.minecraft.gtmqol.gregification.create;

import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.gregification.LoadingTags;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every kind of fan processing, Create's and addons', from Create's registry: which recipes each runs, and what the
 * fan would blow through for it, which our fan takes as inputs it never consumes.
 *
 * <p>Addons follow Create's conventions, so those are what is read: a kind's recipes are the recipe type with its id,
 * its catalysts the blocks (as items) and fluids in the {@code <namespace>:fan_processing_catalysts/<path>} tags.
 * Create's smoking and blasting run vanilla smoking and smelting recipes. By id, without depending on the addon:
 * Dragons Plus freezing also takes its freezer blocks, and its sanding also runs Create's sandpaper polishing, as its
 * fan does. Kinds without a recipe type of their id (Dragons Plus coloring) are left out.</p>
 */
final class FanProcessing {

    private static final String DRAGONS_PLUS = "create_dragons_plus";
    private static final ResourceLocation FREEZING = new ResourceLocation(DRAGONS_PLUS, "freezing");
    private static final ResourceLocation FREEZERS =
            new ResourceLocation(DRAGONS_PLUS, "passive_block_freezers");
    private static final ResourceLocation SANDING = new ResourceLocation(DRAGONS_PLUS, "sanding");

    /** Kinds by recipe type, from common setup on. */
    private static final Map<RecipeType<?>, List<ResourceLocation>> KINDS = new HashMap<>();
    /** By kind, worked out again after the tags change. */
    private static final Map<ResourceLocation, Catalysts> CATALYSTS = new ConcurrentHashMap<>();

    static {
        LoadingTags.onChange(CATALYSTS::clear);
    }

    /**
     * @param items blocks as items, any one of them
     * @param fluid the fluid tag, if it has any
     */
    record Catalysts(List<Item> items, @Nullable TagKey<Fluid> fluid) {}

    private FanProcessing() {}

    /** The recipe types of every kind; at common setup, when every kind is registered. */
    static List<RecipeType<?>> recipeTypes() {
        KINDS.clear();
        for (ResourceLocation kind : CreateBuiltInRegistries.FAN_PROCESSING_TYPE.keySet()) {
            List<RecipeType<?>> types = recipeTypesOf(kind);
            if (types.isEmpty()) {
                GTMQoL.LOGGER.info("The fan leaves out {}: no recipe type with its id", kind);
            }
            for (RecipeType<?> type : types) KINDS.computeIfAbsent(type, t -> new ArrayList<>()).add(kind);
        }
        return List.copyOf(KINDS.keySet());
    }

    private static List<RecipeType<?>> recipeTypesOf(ResourceLocation kind) {
        if (kind.getNamespace().equals("create")) {
            if (kind.getPath().equals("smoking")) return List.of(RecipeType.SMOKING);
            if (kind.getPath().equals("blasting")) return List.of(RecipeType.SMELTING);
        }
        List<RecipeType<?>> types = new ArrayList<>();
        BuiltInRegistries.RECIPE_TYPE.getOptional(kind).ifPresent(types::add);
        if (kind.equals(SANDING)) {
            BuiltInRegistries.RECIPE_TYPE.getOptional(new ResourceLocation("create",
                    "sandpaper_polishing")).ifPresent(types::add);
        }
        return types;
    }

    /** The kinds running recipes of {@code type}. */
    static List<ResourceLocation> kindsOf(RecipeType<?> type) {
        return KINDS.getOrDefault(type, List.of());
    }

    static Catalysts catalysts(ResourceLocation kind) {
        return CATALYSTS.computeIfAbsent(kind, FanProcessing::findCatalysts);
    }

    private static Catalysts findCatalysts(ResourceLocation kind) {
        ResourceLocation tag = kind.withPrefix("fan_processing_catalysts/");
        Set<Item> items = new LinkedHashSet<>();
        addItems(items, tag);
        if (kind.equals(FREEZING)) addItems(items, FREEZERS);
        boolean fluids = !LoadingTags.get(BuiltInRegistries.FLUID, tag).isEmpty();
        return new Catalysts(List.copyOf(items), fluids ? TagKey.create(Registries.FLUID, tag) : null);
    }

    private static void addItems(Set<Item> items, ResourceLocation tag) {
        for (Holder<Block> block : LoadingTags.get(BuiltInRegistries.BLOCK, tag)) {
            Item item = block.value().asItem();
            // fire, lit blaze burners...
            if (item != Items.AIR) items.add(item);
        }
    }
}
