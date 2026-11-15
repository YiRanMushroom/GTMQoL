package com.yiran.minecraft.gtmqol.integration.ae2.crafting;

import java.util.Map;
import java.util.TreeMap;

/**
 * How the crafts of a pattern are cut up for a crafting CPU: as one piece, as far as what takes the pattern allows.
 * Nothing smaller is cut for crafts that wait for their inputs or for power: AE2's CPU pushes the part of a piece
 * that can go ({@link PartialPush}), which the 1, 2, 4.. pieces this used to make only got in the way of (the user,
 * 2026-11-15: on other CPUs they helped by a factor of two at best). No game classes here, so tests can run this
 * without the game.
 */
public final class BatchSizes {

    private BatchSizes() {}

    /**
     * @param total crafts, at least 1
     * @param limit the largest piece allowed, at least 1
     * @return piece size to how many of them
     */
    public static Map<Long, Long> pieces(long total, long limit) {
        Map<Long, Long> pieces = new TreeMap<>();
        if (total >= limit) pieces.put(limit, total / limit);
        if (total % limit > 0) pieces.merge(total % limit, 1L, Long::sum);
        return pieces;
    }
}
