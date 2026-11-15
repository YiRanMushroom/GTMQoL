package com.yiran.minecraft.gtmqol.integration.ae2.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.GenericStack;
import appeng.me.service.CraftingService;

import java.util.HashMap;
import java.util.Map;

/**
 * Pattern batching: makes a crafting plan's crafts of a pattern into batches, when everything that offers the pattern can
 * take them. Every CPU works from the plan, so all of them do this without knowing about it. A CPU pushes a batch once,
 * which is what saves its co-processors the work. A pattern's crafts are one batch, or {@linkplain BatchSizes#pieces as
 * few} as its providers' limits allow.
 */
public final class BatchPlan {

    private BatchPlan() {}

    /** Replaces the crafts in the map. */
    public static void cut(Map<IPatternDetails, Long> crafts, IGridNode node) {
        var service = (CraftingService) node.getGrid().getCraftingService();
        Map<IPatternDetails, Long> cut = new HashMap<>();
        for (var entry : crafts.entrySet()) {
            IPatternDetails pattern = entry.getKey();
            long limit = limit(pattern, service);
            if (entry.getValue() < 2 || limit < 2 || !canBatch(pattern)) {
                cut.merge(pattern, entry.getValue(), Long::sum);
                continue;
            }
            BatchSizes.pieces(entry.getValue(), limit).forEach((size, times) -> {
                cut.merge(PatternBatch.piece(pattern, size), times, Long::sum);
            });
        }
        crafts.clear();
        crafts.putAll(cut);
    }

    /** What all of them take; not with one that doesn't, it would never get any of the crafts. */
    private static long limit(IPatternDetails pattern, CraftingService service) {
        long limit = Long.MAX_VALUE;
        boolean any = false;
        for (var provider : service.getProviders(pattern)) {
            if (!(provider instanceof PatternBatchProvider batches)) return 0;
            limit = Math.min(limit, batches.batchLimit(pattern));
            any = true;
        }
        return any ? limit : 0;
    }

    /**
     * Not a pattern that makes one of its inputs or gives one back: AE2 plans those so that the same items go round,
     * and a batch needs all of them at once.
     */
    private static boolean canBatch(IPatternDetails pattern) {
        for (var input : pattern.getInputs()) {
            for (GenericStack possible : input.getPossibleInputs()) {
                if (input.getRemainingKey(possible.what()) != null) return false;
                for (GenericStack output : pattern.getOutputs()) {
                    if (possible.what().equals(output.what())) return false;
                }
            }
        }
        return true;
    }
}
