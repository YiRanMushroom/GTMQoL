package com.yiran.minecraft.gtmqol.common.crafter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Patterns decoded once per content: decoding a Crafting Pattern checks its recipe, and Pattern Crafters, the
 * Assembling Matrix and tooltips decode the same patterns over and over. Kept per recipe manager: a server reload makes
 * a new one, and the client's is cleared when the server sends new recipes. Callers must not change what they get.
 */
// Untested: Pattern Crafters, the Assembling Matrix and pattern tooltips work as before, and after a /reload that
// changes a recipe the pattern shows as changed
public final class DecodedPatterns {

    /** Per recipe manager; the oldest go first beyond this. */
    private static final int LIMIT = 4096;
    private static final Map<RecipeManager, Map<Key, Optional<ICrafterPattern>>> CACHE = new WeakHashMap<>();

    private DecodedPatterns() {}

    static @Nullable ICrafterPattern get(ItemStack stack, Level level, Function<ItemStack, ICrafterPattern> decode) {
        RecipeManager recipes = level.getRecipeManager();
        Key key = new Key(stack.copyWithCount(1));
        Optional<ICrafterPattern> known;
        synchronized (CACHE) {
            known = byManager(recipes).get(key);
        }
        if (known != null) return known.orElse(null);
        // decoded outside the lock: the client and the integrated server decode at the same time
        ICrafterPattern decoded = decode.apply(stack);
        synchronized (CACHE) {
            byManager(recipes).put(key, Optional.ofNullable(decoded));
        }
        return decoded;
    }

    /** For the client when the server sent new recipes: its recipe manager stays the same object. */
    public static void clear(RecipeManager recipes) {
        synchronized (CACHE) {
            CACHE.remove(recipes);
        }
    }

    private static Map<Key, Optional<ICrafterPattern>> byManager(RecipeManager recipes) {
        return CACHE.computeIfAbsent(recipes, manager -> new LinkedHashMap<>(16, 0.75f, true) {

            @Override
            protected boolean removeEldestEntry(Map.Entry<Key, Optional<ICrafterPattern>> eldest) {
                return size() > LIMIT;
            }
        });
    }

    /** A stack of one, equal to another with the same item and components. */
    private record Key(ItemStack stack) {

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && ItemStack.isSameItemSameComponents(key.stack, stack);
        }

        @Override
        public int hashCode() {
            return ItemStack.hashItemAndComponents(stack);
        }
    }
}
