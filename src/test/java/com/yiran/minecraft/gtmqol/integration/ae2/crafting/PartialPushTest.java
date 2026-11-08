package com.yiran.minecraft.gtmqol.integration.ae2.crafting;

import com.yiran.minecraft.gtmqol.integration.ae2.crafting.PartialPush.Slot;
import com.yiran.minecraft.gtmqol.integration.ae2.crafting.PartialPush.Template;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartialPushTest {

    private static Slot<String> slot(long uses, Object... templates) {
        List<Template<String>> list = new ArrayList<>();
        for (int i = 0; i < templates.length; i += 2) {
            list.add(new Template<>((String) templates[i], ((Number) templates[i + 1]).longValue()));
        }
        return new Slot<>(list, uses);
    }

    @Test
    void allThereIsTheWholeBatch() {
        var slots = List.of(slot(1, "log", 1));
        assertEquals(1000, PartialPush.byInputs(1000, slots, Map.of("log", 4000L)));
    }

    @Test
    void whatHasArrivedGoes() {
        var slots = List.of(slot(2, "plank", 1), slot(1, "stick", 1));
        assertEquals(16, PartialPush.byInputs(1000, slots, Map.of("plank", 33L, "stick", 500L)));
        assertEquals(0, PartialPush.byInputs(1000, slots, Map.of("plank", 33L)));
    }

    @Test
    void templatesTakeWholeUnits() {
        // a bucket's worth per use, the tank holds two and a half
        var slots = List.of(slot(1, "water", 1000));
        assertEquals(2, PartialPush.byInputs(10, slots, Map.of("water", 2500L)));
    }

    @Test
    void substitutesAddUp() {
        var slots = List.of(slot(1, "oak", 1, "birch", 1));
        assertEquals(7, PartialPush.byInputs(10, slots, Map.of("oak", 3L, "birch", 4L)));
    }

    @Test
    void slotsSharingAnItemAreCountedOnce() {
        // two slots both taking any log: 5 logs make 2 crafts, not 5
        var slots = List.of(slot(1, "log", 1), slot(1, "log", 1));
        assertEquals(2, PartialPush.byInputs(10, slots, Map.of("log", 5L)));
    }

    @Test
    void earlierSlotsTakeFirstAsTheCpuDoes() {
        // the first slot takes oak before birch, so the second slot (oak only) is short
        var slots = List.of(slot(1, "oak", 1, "birch", 1), slot(1, "oak", 1));
        assertEquals(PartialPush.byInputs(10, slots, Map.of("oak", 4L, "birch", 10L)),
                linear(10, slots, Map.of("oak", 4L, "birch", 10L)));
    }

    @Test
    void hugeAmountsDontOverflow() {
        var slots = List.of(slot(Long.MAX_VALUE / 2, "dust", 4));
        assertEquals(0, PartialPush.byInputs(Long.MAX_VALUE, slots, Map.of("dust", Long.MAX_VALUE)));
        var single = List.of(slot(1, "dust", 1));
        assertEquals(Long.MAX_VALUE, PartialPush.byInputs(Long.MAX_VALUE, single, Map.of("dust", Long.MAX_VALUE)));
    }

    @Test
    void noSlotsIsNotACraft() {
        assertEquals(0, PartialPush.byInputs(5, List.<Slot<String>>of(), Map.of()));
    }

    @Test
    void randomWorldsMatchCraftByCraft() {
        Random random = new Random(31);
        String[] keys = {"a", "b", "c", "d", "e"};
        for (int world = 0; world < 20000; world++) {
            List<Slot<String>> slots = new ArrayList<>();
            int slotCount = 1 + random.nextInt(4);
            for (int s = 0; s < slotCount; s++) {
                List<Template<String>> templates = new ArrayList<>();
                int templateCount = 1 + random.nextInt(3);
                for (int t = 0; t < templateCount; t++) {
                    templates.add(new Template<>(keys[random.nextInt(keys.length)], 1 + random.nextInt(5)));
                }
                slots.add(new Slot<>(templates, 1 + random.nextInt(4)));
            }
            Map<String, Long> stock = new HashMap<>();
            for (String key : keys) if (random.nextBoolean()) stock.put(key, (long) random.nextInt(200));
            long crafts = 1 + random.nextInt(64);
            long found = PartialPush.byInputs(crafts, slots, stock);
            assertEquals(linear(crafts, slots, stock), found, "slots " + slots + " stock " + stock);
            assertTrue(found <= crafts);
        }
    }

    /** The largest count that fits, trying every one. */
    private static long linear(long crafts, List<Slot<String>> slots, Map<String, Long> stock) {
        long best = 0;
        for (long k = 1; k <= crafts; k++) if (PartialPush.fits(k, slots, stock)) best = k;
        return best;
    }

    @Test
    void powerInProportion() {
        assertEquals(500, PartialPush.byPower(1000, 2000, 1000));
        assertEquals(999, PartialPush.byPower(1000, 1000, 999.99));
        assertEquals(0, PartialPush.byPower(1000, 1000, 0.5));
        assertEquals(0, PartialPush.byPower(1000, 1000, 0));
    }

    @Test
    void powerAlwaysLessThanTried() {
        // a check that failed by a hair must still go smaller, or it would be tried again forever
        assertEquals(9, PartialPush.byPower(10, 10, 10));
        assertEquals(9, PartialPush.byPower(10, 10, 20));
        Random random = new Random(32);
        for (int i = 0; i < 100000; i++) {
            long crafts = 1 + (random.nextLong() >>> (1 + random.nextInt(62)));
            double power = random.nextDouble() * crafts * 4;
            double available = random.nextDouble() * power;
            long next = PartialPush.byPower(crafts, power, available);
            assertTrue(next >= 0 && next < crafts, crafts + " " + power + " " + available);
        }
    }
}
