package com.yiran.minecraft.gtmqol.integration.ae2.batch;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchSizesTest {

    private static final int PARTS = 16;

    private static long sum(Map<Long, Long> pieces) {
        return pieces.entrySet().stream().mapToLong(piece -> piece.getKey() * piece.getValue()).sum();
    }

    private static long count(Map<Long, Long> pieces) {
        return pieces.values().stream().mapToLong(Long::longValue).sum();
    }

    @Test
    void oneCraftAtTheDefault() {
        assertEquals(Map.of(1L, 1L), BatchSizes.split(1, Long.MAX_VALUE, 1));
    }

    @Test
    void theDefaultIsDoublingAndTheRest() {
        var pieces = BatchSizes.split(1000, Long.MAX_VALUE, 1);
        assertEquals(1000, sum(pieces));
        for (long size = 1; size <= 256; size *= 2) assertEquals(1L, pieces.get(size), "piece of " + size);
        assertEquals(1L, pieces.get(489L));
        assertEquals(10, pieces.size());
    }

    @Test
    void sizesNeverRepeatAtTheDefault() {
        Random random = new Random(22);
        for (long total = 1; total < 5000; total++) assertNoRepeats(total);
        for (int i = 0; i < 100000; i++) assertNoRepeats(1 + (random.nextLong() >>> (1 + random.nextInt(62))));
    }

    private static void assertNoRepeats(long total) {
        var pieces = BatchSizes.split(total, Long.MAX_VALUE, 1);
        assertEquals(total, sum(pieces), "total " + total);
        assertTrue(pieces.values().stream().allMatch(times -> times == 1), "repeats in " + total + ": " + pieces);
        assertTrue(pieces.size() <= 64, "too many pieces for " + total);
    }

    @Test
    void wholeIsOnePieceWithoutALimit() {
        assertEquals(Map.of(1000L, 1L), BatchSizes.whole(1000, Long.MAX_VALUE));
        assertEquals(Map.of(1L, 1L), BatchSizes.whole(1, Long.MAX_VALUE));
        assertEquals(Map.of(Long.MAX_VALUE, 1L), BatchSizes.whole(Long.MAX_VALUE, Long.MAX_VALUE));
    }

    @Test
    void wholeKeepsToTheLimit() {
        assertEquals(Map.of(100L, 10L), BatchSizes.whole(1000, 100));
        assertEquals(Map.of(100L, 10L, 7L, 1L), BatchSizes.whole(1007, 100));
        assertEquals(Map.of(7L, 1L), BatchSizes.whole(7, 100));
        Random random = new Random(23);
        for (int i = 0; i < 100000; i++) {
            long total = 1 + (random.nextLong() >>> (1 + random.nextInt(62)));
            long limit = 2 + (random.nextLong() >>> (2 + random.nextInt(61)));
            var pieces = BatchSizes.whole(total, limit);
            assertEquals(total, sum(pieces));
            assertTrue(pieces.keySet().stream().allMatch(size -> size >= 1 && size <= limit));
        }
    }

    @Test
    void oneCraft() {
        assertEquals(Map.of(1L, 1L), BatchSizes.split(1, Long.MAX_VALUE, PARTS));
    }

    @Test
    void fewCraftsGoOneByOne() {
        assertEquals(Map.of(1L, 16L), BatchSizes.split(16, Long.MAX_VALUE, PARTS));
    }

    @Test
    void thousandCrafts() {
        var pieces = BatchSizes.split(1000, Long.MAX_VALUE, PARTS);
        assertEquals(1000, sum(pieces));
        // small ones first for what arrives early
        for (long size = 1; size < 64; size *= 2) assertTrue(pieces.containsKey(size), "no piece of " + size);
        assertEquals(14L, pieces.get(64L));
        assertTrue(count(pieces) < 30);
    }

    @Test
    void limitIsTheLargestPiece() {
        var pieces = BatchSizes.split(1000, 10, PARTS);
        assertEquals(1000, sum(pieces));
        assertTrue(pieces.keySet().stream().allMatch(size -> size <= 10));
        assertEquals(10L, pieces.keySet().stream().max(Long::compare).orElseThrow());
    }

    @Test
    void limitOfOneIsCraftByCraft() {
        assertEquals(Map.of(1L, 500L), BatchSizes.split(500, 1, PARTS));
    }

    @Test
    void randomTotals() {
        Random random = new Random(21);
        for (int i = 0; i < 200000; i++) {
            long total = 1 + (random.nextLong() >>> (1 + random.nextInt(62)));
            long limit = random.nextBoolean() ? Long.MAX_VALUE : 1 + (random.nextLong() >>> (1 + random.nextInt(62)));
            int parts = 1 + random.nextInt(64);
            var pieces = BatchSizes.split(total, limit, parts);
            assertEquals(total, sum(pieces), "total " + total + " limit " + limit + " parts " + parts);
            assertTrue(pieces.keySet().stream().allMatch(size -> size >= 1 && size <= limit));
            assertTrue(pieces.values().stream().allMatch(times -> times >= 1));
            if (limit == Long.MAX_VALUE) assertTrue(count(pieces) <= parts + 2L * 64, "too many pieces for " + total);
        }
    }

    @Test
    void largestPossibleTotal() {
        assertEquals(Long.MAX_VALUE, sum(BatchSizes.split(Long.MAX_VALUE, Long.MAX_VALUE, 1)));
    }
}
