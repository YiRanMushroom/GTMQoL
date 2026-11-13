package com.yiran.minecraft.gtmqol.common.ghost;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.List;

/**
 * A recipe's ghost ingredient that any marked item or fluid of a tag satisfies ({@link #isSatisfiedBy}). Recipes hold
 * these; marks are always single items and fluids, which stand for their tags in the recipe lookup
 * ({@link GhostIngredient#lookupContents}). Nothing is resolved when the recipe is made, so they are safe to create
 * while recipes load.
 *
 * @param <T> what the tag holds, items or fluids
 */
// Untested: a recipe with a tag ghost runs with any item or fluid of the tag marked (a gregified MI distillery
// recipe takes GT's fluid or a pattern making it), and EMI shows the tag's entries in the ghost slot
public abstract class TagGhostIngredient<T> extends GhostIngredient<TagKey<T>> {

    protected TagGhostIngredient(TagKey<T> tag) {
        super(tag);
    }

    @SuppressWarnings("unchecked")
    protected static <T> Class<TagKey<T>> tagClass() {
        return (Class<TagKey<T>>) (Class<?>) TagKey.class;
    }

    protected static <T> void write(FriendlyByteBuf buf, TagKey<T> tag) {
        buf.writeResourceLocation(tag.location());
    }

    protected static <T> FriendlyByteBuf.Reader<TagKey<T>> reader(ResourceKey<? extends Registry<T>> registry) {
        return buf -> TagKey.create(registry, buf.readResourceLocation());
    }

    @Override
    protected boolean sameValue(TagKey<T> other) {
        return value.equals(other);
    }

    @Override
    protected int valueHash() {
        return value.hashCode();
    }

    /**
     * The tag's own name where a mod gives one (the tag translation keys NeoForge has on 1.21:
     * {@code tag.item.c.ingots.iron}), else {@code #namespace:path}.
     */
    @Override
    public Component getName() {
        String key = "tag." + value.registry().location().getPath().replace('/', '.') + "." +
                value.location().getNamespace() + "." + value.location().getPath().replace('/', '.');
        return Component.translatableWithFallback(key, "#" + value.location());
    }

    /** What is in the tag now, for the icon; empty before tags are bound. */
    protected static <T> List<Holder<T>> contents(Registry<T> registry, TagKey<T> tag) {
        return registry.getTag(tag).map(set -> set.stream().toList()).orElse(List.of());
    }

    /** One of the tag's entries, a new one every second, so the icon cycles through them like a recipe viewer. */
    protected static <T> Holder<T> current(List<Holder<T>> contents) {
        return contents.get((int) (System.currentTimeMillis() / 1000 % contents.size()));
    }
}
