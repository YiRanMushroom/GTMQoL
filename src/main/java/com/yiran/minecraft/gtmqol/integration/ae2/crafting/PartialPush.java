package com.yiran.minecraft.gtmqol.integration.ae2.crafting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * How many crafts of a batch AE2's CPU can push now, when it can't push all of them: inputs still being made upstream,
 * or too little AE power. The rest stays a task of its own. No game classes here, so tests can run this without the game.
 */
public final class PartialPush {

    private PartialPush() {}

    /** What fits one use of an input, and how much of it one use takes. */
    public record Template<K>(K key, long amount) {}

    /** An input of the pattern: what fits it in the order the CPU tries them, and how many uses one craft takes. */
    public record Slot<K>(List<Template<K>> templates, long usesPerCraft) {}

    /**
     * The most crafts, up to {@code crafts}, whose inputs the CPU would get out of {@code stock}: it fills the inputs in
     * order, each from its templates in order, taking whole uses of as much as there is.
     */
    public static <K> long byInputs(long crafts, List<Slot<K>> slots, Map<K, Long> stock) {
        if (slots.isEmpty()) return 0;
        long high = Math.min(crafts, upperBound(slots, stock));
        // usually all that is there fits, so try that first
        if (high == 0 || fits(high, slots, stock)) return high;
        // taking for fewer crafts never leaves less for a later input, so what fits is everything up to a point
        long low = 0;
        high--;
        while (low < high) {
            long middle = high - (high - low) / 2;
            if (fits(middle, slots, stock)) low = middle;
            else high = middle - 1;
        }
        return low;
    }

    /**
     * No more crafts than each input alone could get, counting every template's whole stock for it: inputs that share a
     * key can only get less.
     */
    static <K> long upperBound(List<Slot<K>> slots, Map<K, Long> stock) {
        long bound = Long.MAX_VALUE;
        for (Slot<K> slot : slots) {
            long uses = 0;
            for (Template<K> template : slot.templates()) {
                long there = stock.getOrDefault(template.key(), 0L) / template.amount();
                uses = there > Long.MAX_VALUE - uses ? Long.MAX_VALUE : uses + there;
            }
            bound = Math.min(bound, uses / slot.usesPerCraft());
            if (bound == 0) return 0;
        }
        return bound;
    }

    /** Whether the CPU would get every input of {@code crafts} crafts. */
    static <K> boolean fits(long crafts, List<Slot<K>> slots, Map<K, Long> stock) {
        Map<K, Long> left = new HashMap<>(stock);
        for (Slot<K> slot : slots) {
            if (slot.usesPerCraft() > Long.MAX_VALUE / crafts) return false;
            long uses = slot.usesPerCraft() * crafts;
            for (Template<K> template : slot.templates()) {
                long there = left.getOrDefault(template.key(), 0L);
                long taken = Math.min(uses, there / template.amount());
                left.put(template.key(), there - taken * template.amount());
                uses -= taken;
                if (uses == 0) break;
            }
            if (uses > 0) return false;
        }
        return true;
    }

    /**
     * After {@code crafts} crafts needing {@code power} failed the power check: as many as {@code available} pays for,
     * and always fewer than before, so a check that misses by a hair doesn't come back to the same count.
     */
    public static long byPower(long crafts, double power, double available) {
        if (available <= 0 || power <= 0) return 0;
        double affordable = Math.floor(crafts * (available / power));
        return Math.max(0, Math.min(crafts - 1, affordable >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) affordable));
    }
}
