package com.yiran.minecraft.gtmqol.gregification;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tags for converting recipes. A server reload loads recipes before the new tags are bound to the registries, so
 * until they are, this reads them from the reload's condition context, Forge's way of giving recipe loading the
 * tags. Otherwise (bound by now, or a client converting synced recipes) the registries' own.
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
}
