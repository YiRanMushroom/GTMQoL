package com.yiran.minecraft.gtmqol.integration.ae2.batch;

import java.util.Map;
import java.util.TreeMap;

/**
 * How the crafts of a pattern are cut up for a crafting CPU. A push waits until all its inputs are there, and until there
 * is power for all of it. So crafts that have to wait for something go out in small pieces first, 1, 2, 4 and so on,
 * which leave with whatever has arrived. A CPU pushes one piece of a size per round, so sizes don't repeat. No game
 * classes here, so tests can run this without the game.
 */
public final class BatchPieces {

    private BatchPieces() {}

    /**
     * For crafts that have to wait for something.
     *
     * @param total crafts, at least 1
     * @param limit the largest piece allowed, at least 1
     * @param parts above 1: the largest piece is about this fraction of the total
     * @return piece size to how many of them, smallest first
     */
    public static Map<Long, Long> split(long total, long limit, int parts) {
        long largest = Math.min(limit, roundUpToPowerOfTwo(Math.max(1, total / parts + (total % parts == 0 ? 0 : 1))));
        Map<Long, Long> pieces = new TreeMap<>();
        long left = total;
        // what is left stays larger than the last piece, so it can go as one without repeating a size
        for (long size = 1; size < largest && left - size > size; size *= 2) {
            pieces.put(size, 1L);
            left -= size;
        }
        if (left > largest) {
            pieces.merge(largest, left / largest, Long::sum);
            left %= largest;
        }
        if (left > 0) pieces.merge(left, 1L, Long::sum);
        return pieces;
    }

    /** For crafts that have all they need: as one, as far as the limit allows. */
    public static Map<Long, Long> whole(long total, long limit) {
        Map<Long, Long> pieces = new TreeMap<>();
        if (total >= limit) pieces.put(limit, total / limit);
        if (total % limit > 0) pieces.merge(total % limit, 1L, Long::sum);
        return pieces;
    }

    private static long roundUpToPowerOfTwo(long value) {
        long power = Long.highestOneBit(value);
        if (power == value) return value;
        return power >= Long.MAX_VALUE / 2 ? power : power * 2;
    }
}
