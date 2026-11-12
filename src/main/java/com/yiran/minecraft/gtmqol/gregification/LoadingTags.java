package com.yiran.minecraft.gtmqol.gregification;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tags for converting recipes. A server reload loads recipes before the new tags are bound to the registries, so
 * until they are, this reads them from the reload's condition context, Forge's way of giving recipe loading the
 * tags. Otherwise (bound by now, or a client converting synced recipes) the registries' own.
 * <p>
 * What another mod's ingredient takes is read here too ({@link #items}), never with its own {@code getItems},
 * {@code test} or {@code getStackingIds} (nor Mekanism's {@code isIncomplete}): those keep what they find for good,
 * so asked before the tags are bound, the ingredient, which the other mod's recipe holds too, takes nothing from a
 * tag until the next reload. Mekanism's own machines then didn't take tagged inputs after a world loaded.
 */
public final class LoadingTags {

    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();
    private static @Nullable ICondition.IContext context;

    private LoadingTags() {}

    public static void init() {
        // before anything reacting to the same events reads tags
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, AddReloadListenerEvent.class,
                event -> changed(event.getConditionContext()));
        // bound now, on the server or on a client receiving them
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, TagsUpdatedEvent.class,
                event -> changed(null));
    }

    private static void changed(@Nullable ICondition.IContext newContext) {
        synchronized (LoadingTags.class) {
            context = newContext;
        }
        LISTENERS.forEach(Runnable::run);
    }

    /** Runs whenever the tags may have changed, e.g. to drop what was worked out from them. */
    public static void onChange(Runnable listener) {
        LISTENERS.add(listener);
    }

    /** Every tag of {@code registry}, by id. */
    public static synchronized <T> Map<ResourceLocation, Collection<Holder<T>>> all(Registry<T> registry) {
        if (context != null) {
            try {
                return context.getAllTags(registry.key());
            } catch (IllegalStateException e) {
                // tags not loaded yet; recipes come after them, so this shouldn't happen
                return Map.of();
            }
        }
        Map<ResourceLocation, Collection<Holder<T>>> tags = new HashMap<>();
        registry.getTags().forEach(pair -> tags.put(pair.getFirst().location(), pair.getSecond().stream().toList()));
        return tags;
    }

    /** What is in one tag of {@code registry}, nothing if it doesn't exist. */
    public static synchronized <T> Collection<Holder<T>> get(Registry<T> registry, ResourceLocation tag) {
        if (context != null) return all(registry).getOrDefault(tag, List.of());
        return registry.getTag(TagKey.create(registry.key(), tag))
                .<Collection<Holder<T>>>map(set -> set.stream().toList())
                .orElse(List.of());
    }

    /** What is in one tag while recipes load, by the reload's tags; null outside a reload (the registries have them). */
    public static synchronized <T> @Nullable Collection<Holder<T>> loading(TagKey<T> tag) {
        if (context == null) return null;
        try {
            return context.getAllTags(tag.registry()).getOrDefault(tag.location(), List.of());
        } catch (IllegalStateException e) {
            return List.of();
        }
    }

    /**
     * The items an ingredient takes, from how it is written and these tags (see the class comment). Null for a
     * custom ingredient other than a compound one, which only its own code can tell.
     */
    // Untested: launch the game and load a world: Mekanism's enrichment chamber and metallurgic infuser take tagged
    // inputs (iron ore, iron ingot), Create's saw too; EMI lists the gregified Mekanism machines' recipes with tag
    // inputs
    public static @Nullable Set<Item> items(Ingredient ingredient) {
        Set<Item> items = new HashSet<>();
        if (ingredient instanceof CompoundIngredient compound) {
            for (Ingredient child : compound.getChildren()) {
                Set<Item> part = items(child);
                if (part == null) return null;
                items.addAll(part);
            }
            return items;
        }
        if (!ingredient.isVanilla()) return null;
        // no getter for the values on 1.20: each one is written as {"item": ...} or {"tag": ...}
        JsonElement json = ingredient.toJson();
        Iterable<JsonElement> values = json.isJsonArray() ? json.getAsJsonArray() : List.of(json);
        for (JsonElement value : values) {
            if (!(value instanceof JsonObject object)) continue;
            if (object.has("item")) {
                ResourceLocation id = ResourceLocation.tryParse(GsonHelper.getAsString(object, "item"));
                if (id != null) BuiltInRegistries.ITEM.getOptional(id).ifPresent(items::add);
            } else if (object.has("tag")) {
                ResourceLocation id = ResourceLocation.tryParse(GsonHelper.getAsString(object, "tag"));
                if (id != null) get(BuiltInRegistries.ITEM, id).forEach(holder -> items.add(holder.value()));
            }
        }
        return items;
    }
}
