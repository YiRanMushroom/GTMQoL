package com.yiran.minecraft.gtmqol.gregification;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;

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
 * until they are, this reads them from the reload's condition context, NeoForge's way of giving recipe loading the
 * tags. Otherwise (bound by now, or a client converting synced recipes) the registries' own.
 * <p>
 * What another mod's ingredient takes is read here too ({@link #items}, {@link #fluids}), never with its own
 * {@code getItems}, {@code test}, {@code hasNoItems} or {@code getStacks} (nor Mekanism's {@code isIncomplete}): those
 * keep what they find for good, so asked before the tags are bound, the ingredient, which the other mod's recipe holds
 * too, takes nothing from a tag until the next reload. Mekanism's and MI's own machines then didn't take tagged
 * inputs after a world loaded, and Mekanism's scan for incomplete recipes crashed the server.
 */
public final class LoadingTags {

    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();
    private static @Nullable ICondition.IContext context;

    private LoadingTags() {}

    public static void init() {
        // before anything reacting to the same events reads tags
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, AddReloadListenerEvent.class,
                event -> changed(event.getConditionContext()));
        // bound now, on the server or on a client receiving them
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, TagsUpdatedEvent.class, event -> changed(null));
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

    /**
     * The items an ingredient takes, from its values and these tags (see the class comment). Null for a custom
     * ingredient other than a compound one, which only its own code can tell.
     */
    // Untested: launch the game and load a world: no "Incomplete recipe detected" errors from Mekanism and no crash on
    // its scan; Mekanism's enrichment chamber and metallurgic infuser take tagged inputs (iron ore, iron ingot), MI's
    // macerator and Create's saw too; EMI lists the gregified Mekanism machines' recipes with tag inputs
    public static @Nullable Set<Item> items(Ingredient ingredient) {
        Set<Item> items = new HashSet<>();
        if (ingredient.getCustomIngredient() instanceof CompoundIngredient compound) {
            for (Ingredient child : compound.children()) {
                Set<Item> part = items(child);
                if (part == null) return null;
                items.addAll(part);
            }
            return items;
        }
        if (ingredient.isCustom()) return null;
        for (Ingredient.Value value : ingredient.getValues()) {
            if (value instanceof Ingredient.ItemValue(ItemStack stack)) {
                items.add(stack.getItem());
            } else if (value instanceof Ingredient.TagValue(TagKey<Item> tag)) {
                get(BuiltInRegistries.ITEM, tag.location()).forEach(holder -> items.add(holder.value()));
            }
        }
        return items;
    }

    /** Same as {@link #items}, for a fluid ingredient: single fluids, tags and compounds of them. */
    public static @Nullable Set<Fluid> fluids(FluidIngredient ingredient) {
        Set<Fluid> fluids = new HashSet<>();
        switch (ingredient) {
            case SingleFluidIngredient single -> fluids.add(single.fluid().value());
            case TagFluidIngredient tagged -> get(BuiltInRegistries.FLUID, tagged.tag().location())
                    .forEach(holder -> fluids.add(holder.value()));
            case CompoundFluidIngredient compound -> {
                for (FluidIngredient child : compound.children()) {
                    Set<Fluid> part = fluids(child);
                    if (part == null) return null;
                    fluids.addAll(part);
                }
            }
            default -> {
                return null;
            }
        }
        return fluids;
    }
}
