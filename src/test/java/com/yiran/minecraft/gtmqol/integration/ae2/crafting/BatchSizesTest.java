package com.yiran.minecraft.gtmqol.integration.ae2.crafting;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchSizesTest {

    private static long sum(Map<Long, Long> pieces) {
        return pieces.entrySet().stream().mapToLong(piece -> piece.getKey() * piece.getValue()).sum();
    }

    @Test
    void onePieceWithoutALimit() {
        assertEquals(Map.of(1000L, 1L), BatchSizes.pieces(1000, Long.MAX_VALUE));
        assertEquals(Map.of(1L, 1L), BatchSizes.pieces(1, Long.MAX_VALUE));
        assertEquals(Map.of(Long.MAX_VALUE, 1L), BatchSizes.pieces(Long.MAX_VALUE, Long.MAX_VALUE));
    }

    @Test
    void keepsToTheLimit() {
        assertEquals(Map.of(100L, 10L), BatchSizes.pieces(1000, 100));
        assertEquals(Map.of(100L, 10L, 7L, 1L), BatchSizes.pieces(1007, 100));
        assertEquals(Map.of(7L, 1L), BatchSizes.pieces(7, 100));
        assertEquals(Map.of(1L, 500L), BatchSizes.pieces(500, 1));
        Random random = new Random(23);
        for (int i = 0; i < 100000; i++) {
            long total = 1 + (random.nextLong() >>> (1 + random.nextInt(62)));
            long limit = 1 + (random.nextLong() >>> (2 + random.nextInt(61)));
            var pieces = BatchSizes.pieces(total, limit);
            assertEquals(total, sum(pieces));
            assertTrue(pieces.keySet().stream().allMatch(size -> size >= 1 && size <= limit));
        }
    }
}
