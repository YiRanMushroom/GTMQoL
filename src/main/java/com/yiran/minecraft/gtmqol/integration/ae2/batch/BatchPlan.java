package com.yiran.minecraft.gtmqol.integration.ae2.batch;

import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.me.service.CraftingService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Smart doubling: makes a crafting plan's crafts of a pattern into batches, when everything that offers the pattern can
 * take them. Every CPU works from the plan, so all of them do this without knowing about it. A CPU pushes a batch once,
 * which is what saves its co-processors the work. Crafts go as one when they have all they need from the start and there
 * is power for them, else in {@linkplain BatchPieces#split pieces} that leave with what has arrived.
 */
public final class BatchPlan {

    private BatchPlan() {}

    /**
     * Replaces the crafts in the map.
     *
     * @param emitted what level emitters will make for the plan, which comes in over time
     */
    public static void cut(Map<IPatternDetails, Long> crafts, Set<AEKey> emitted, IGridNode node) {
        var service = (CraftingService) node.getGrid().getCraftingService();
        var registries = node.getLevel().registryAccess();
        int parts = GTMQoLConfig.get().ae2.batchParts;
        IEnergyService energy = node.getGrid().getEnergyService();
        Set<AEKey> arriving = arriving(crafts, emitted);
        Map<IPatternDetails, Long> cut = new HashMap<>();
        for (var entry : crafts.entrySet()) {
            IPatternDetails pattern = entry.getKey();
            long limit = limit(pattern, service);
            if (entry.getValue() < 2 || limit < 2 || !canBatch(pattern)) {
                cut.merge(pattern, entry.getValue(), Long::sum);
                continue;
            }
            long total = entry.getValue();
            boolean whole = !waitsFor(pattern, arriving) && hasPowerFor(pattern, total, energy);
            (whole ? BatchPieces.whole(total, limit) : BatchPieces.split(total, limit, parts)).forEach((size, times) -> {
                cut.merge(PatternBatch.piece(pattern, size, registries), times, Long::sum);
            });
        }
        crafts.clear();
        crafts.putAll(cut);
    }

    /** Everything that turns up while the job runs, instead of being there from the start. */
    private static Set<AEKey> arriving(Map<IPatternDetails, Long> crafts, Set<AEKey> emitted) {
        Set<AEKey> arriving = new HashSet<>(emitted);
        for (IPatternDetails pattern : crafts.keySet()) {
            for (GenericStack output : pattern.getOutputs()) arriving.add(output.what());
        }
        return arriving;
    }

    private static boolean waitsFor(IPatternDetails pattern, Set<AEKey> arriving) {
        for (var input : pattern.getInputs()) {
            for (GenericStack possible : input.getPossibleInputs()) {
                if (arriving.contains(possible.what())) return true;
            }
        }
        return false;
    }

    /** As the CPU works it out for a push: it won't start on one it can't pay for in full. */
    private static boolean hasPowerFor(IPatternDetails pattern, long crafts, IEnergyService energy) {
        double power = 0;
        for (var input : pattern.getInputs()) {
            GenericStack first = input.getPossibleInputs()[0];
            power += (double) first.amount() * input.getMultiplier() * crafts / first.what().getAmountPerOperation();
        }
        return energy.extractAEPower(power, Actionable.SIMULATE, PowerMultiplier.CONFIG) >= power - 0.01;
    }

    /** What all of them take; not with one that doesn't, it would never get any of the crafts. */
    private static long limit(IPatternDetails pattern, CraftingService service) {
        long limit = Long.MAX_VALUE;
        boolean any = false;
        for (var provider : service.getProviders(pattern)) {
            if (!(provider instanceof BatchCraftingProvider batches)) return 0;
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
