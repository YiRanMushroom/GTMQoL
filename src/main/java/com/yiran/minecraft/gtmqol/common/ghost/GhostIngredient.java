package com.yiran.minecraft.gtmqol.common.ghost;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Something marked in a {@link GhostIngredientHatchPartMachine}: a {@link Type} (its id is a string, which
 * (de)serialization dispatches on) and a value, e.g. {@link ItemGhostIngredient} with an {@link ItemStack}. Two are
 * equal when they mark the same thing: same type, same value apart from its amount. Immutable.
 *
 * <p>Subclasses say how they look (icon, tooltip, recipe viewer entries); the list, the recipe viewers and the
 * recipe viewer drags only go through these, so a new kind is a subclass and a registered {@link Type}.</p>
 *
 * @param <T> the value
 */
public abstract class GhostIngredient<T> {

    /** In registration order, which is also the order kinds are tried in when recognising something. */
    private static final Map<String, Type<?>> TYPES = new LinkedHashMap<>();

    public static final Codec<GhostIngredient<?>> CODEC = Codec.STRING.partialDispatch("type",
            ghost -> DataResult.success(ghost.type().id()), GhostIngredient::codec);

    protected final T value;

    protected GhostIngredient(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    public abstract Type<T> type();

    /** Whether {@code other} marks the same thing, ignoring its amount. */
    protected abstract boolean sameValue(T other);

    /** Consistent with {@link #sameValue}. */
    protected abstract int valueHash();

    public abstract Component getName();

    public List<Component> getTooltip() {
        return List.of(getName());
    }

    /** The value for recipe viewers: slots, and EMI through ModularUI's converters. */
    public abstract EntryList<T> toEntryList();

    /** Background of its recipe viewer slot. */
    public abstract IDrawable slotBackground();

    /** A 16 × 16 icon at {@code x, y}. */
    @OnlyIn(Dist.CLIENT)
    public abstract void drawIcon(GuiGraphics graphics, int x, int y);

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(Object o) {
        return o instanceof GhostIngredient<?> ghost && ghost.type() == type() && sameValue((T) ghost.value);
    }

    @Override
    public final int hashCode() {
        return 31 * type().id().hashCode() + valueHash();
    }

    @Override
    public String toString() {
        return type().id() + ":" + value;
    }

    private void writeValue(FriendlyByteBuf buf) {
        type().valueWriter().accept(buf, value);
    }

    /** Over the network: the type id, then the value. */
    public static void write(FriendlyByteBuf buf, GhostIngredient<?> ghost) {
        buf.writeUtf(ghost.type().id());
        ghost.writeValue(buf);
    }

    public static GhostIngredient<?> read(FriendlyByteBuf buf) {
        return type(buf.readUtf()).read(buf);
    }

    /** Registers a type; its id must be unique. */
    public static <T> Type<T> register(Type<T> type) {
        if (TYPES.putIfAbsent(type.id(), type) != null) {
            throw new IllegalArgumentException("Ghost ingredient type " + type.id() + " registered twice");
        }
        return type;
    }

    public static Collection<Type<?>> types() {
        return Collections.unmodifiableCollection(TYPES.values());
    }

    /** What clicking with {@code held} marks: the first kind that reads something from it. */
    public static @Nullable GhostIngredient<?> fromHeld(ItemStack held, boolean rightClick) {
        for (Type<?> type : TYPES.values()) {
            GhostIngredient<?> ghost = type.fromHeld(held, rightClick);
            if (ghost != null) return ghost;
        }
        return null;
    }

    /** The ghost ingredient of a value of any kind (an item stack, a fluid stack...), or null. */
    public static @Nullable GhostIngredient<?> ofValue(Object value) {
        for (Type<?> type : TYPES.values()) {
            GhostIngredient<?> ghost = type.ofInstance(value);
            if (ghost != null) return ghost;
        }
        return null;
    }

    private static Type<?> type(String id) {
        Type<?> type = TYPES.get(id);
        if (type == null) throw new IllegalArgumentException("Unknown ghost ingredient type " + id);
        return type;
    }

    // A MapCodec's codec, so the dispatch puts the value next to the type.
    private static DataResult<Codec<? extends GhostIngredient<?>>> codec(String id) {
        Type<?> type = TYPES.get(id);
        return type == null ? DataResult.error(() -> "Unknown ghost ingredient type " + id) :
                DataResult.success(type.mapCodec().codec());
    }

    /** Reads what clicking with an item marks, or null. */
    @FunctionalInterface
    public interface HeldReader<T> {

        @Nullable
        T read(ItemStack held, boolean rightClick);
    }

    /**
     * @param id          the type string written with every ghost ingredient
     * @param valueClass  also how recipe viewer ingredients and ModularUI's EMI converters are found
     * @param valueCodec  the value, saved and in recipes
     * @param valueWriter the value, over the network
     * @param valueReader the value, over the network
     * @param isEmpty     values that mark nothing
     * @param factory     makes the ghost ingredient of a value
     * @param heldReader  what clicking with an item marks
     */
    public record Type<T>(String id, Class<T> valueClass, Codec<T> valueCodec, FriendlyByteBuf.Writer<T> valueWriter,
                          FriendlyByteBuf.Reader<T> valueReader, Predicate<T> isEmpty,
                          Function<T, ? extends GhostIngredient<T>> factory, HeldReader<T> heldReader) {

        /** The ghost ingredient of a value, or null if it is empty. */
        public @Nullable GhostIngredient<T> of(@Nullable T value) {
            return value == null || isEmpty.test(value) ? null : factory.apply(value);
        }

        public @Nullable GhostIngredient<T> fromHeld(ItemStack held, boolean rightClick) {
            return of(heldReader.read(held, rightClick));
        }

        private @Nullable GhostIngredient<T> ofInstance(Object value) {
            return valueClass.isInstance(value) ? of(valueClass.cast(value)) : null;
        }

        private MapCodec<GhostIngredient<T>> mapCodec() {
            return valueCodec.fieldOf("value").xmap(factory::apply, GhostIngredient::value);
        }

        private GhostIngredient<T> read(FriendlyByteBuf buf) {
            return factory.apply(valueReader.apply(buf));
        }
    }
}
