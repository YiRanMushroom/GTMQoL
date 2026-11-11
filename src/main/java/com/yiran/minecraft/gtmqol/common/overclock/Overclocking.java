package com.yiran.minecraft.gtmqol.common.overclock;

import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

/**
 * Replacement factors for gtceu's four overclock constants. The constants themselves are
 * {@code static final} interface fields built from inlined compile-time doubles, so instead of patching
 * them, {@code OverclockingLogicMixin.getModifier} and {@code GTRecipeViewerWidgetMixin} swap them out
 * through {@link #replace} at the call sites.
 */
public final class Overclocking {

    /** 4x EU/t, 4x speed per overclock. */
    public static final OverclockingLogic STANDARD = OverclockingLogic.create(0.25, 4.0, true);
    /** 2x EU/t, 4x speed per overclock. */
    public static final OverclockingLogic PERFECT = OverclockingLogic.create(0.25, 2.0, true);

    /**
     * The most parallels we ask GTCEu's {@code ParallelLogic} for. Not {@link Integer#MAX_VALUE}: {@code getMaxByInput}
     * starts from that and takes it for "no input capability" ("machine has no capabilities: import") when every input
     * allows that many, as a fluid input above 2.1G mB does at a high enough voltage.
     */
    // Untested: the gregified rotary condensentrator at UHV max amperage with over 2.1G mB of input fluid runs
    public static final int MAX_PARALLELS = 2_000_000_000;

    private Overclocking() {}

    /**
     * How many crafts a run does: its parallels of every kind multiplied, at most {@link #MAX_PARALLELS}. Each of
     * the three is capped there, their plain int product wraps.
     */
    public static int crafts(GTRecipe recipe) {
        long crafts = Math.min(MAX_PARALLELS, (long) recipe.parallels * recipe.subtickParallels);
        return (int) Math.min(MAX_PARALLELS, crafts * recipe.batchParallels);
    }

    public static OverclockingLogic replace(OverclockingLogic logic) {
        if (logic == OverclockingLogic.NON_PERFECT_OVERCLOCK || logic == OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK) {
            return STANDARD;
        }
        if (logic == OverclockingLogic.PERFECT_OVERCLOCK || logic == OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK) {
            return PERFECT;
        }
        return logic;
    }

    /**
     * Equivalent overclocking, each overclock costing {@code voltageFactor} EU/t: the first {@code fastOCs} gain
     * {@code fastSpeedFactor} speed, the rest {@code speedFactor}. The last one used may be partial (a 5 tick recipe
     * goes to 1 tick), and once a recipe is at 1 tick only whole parallels count, so a recipe whose inputs allow few
     * parallels still reaches 1 tick instead of stopping where the next whole overclock doesn't fit.
     */
    public static OverclockingLogic.OCResult equivalentOC(OverclockingLogic.OCParams params, double voltageFactor,
                                                          double speedFactor, int fastOCs, double fastSpeedFactor) {
        double initialDuration = params.duration();
        int maxParallels = params.maxParallels();
        if (maxParallels < 0) return new OverclockingLogic.OCResult(1, 1, 0, 1);
        if (maxParallels == 0) maxParallels = 1;

        int fast = Math.max(0, Math.min(fastOCs, params.ocAmount()));
        double fastSpeedGain = Math.pow(fastSpeedFactor, fast);
        double maxSpeedGain = fastSpeedGain * Math.pow(speedFactor, params.ocAmount() - fast);
        double absoluteMaxSpeedGain = initialDuration * maxParallels;

        double speedGain;
        if (maxSpeedGain >= absoluteMaxSpeedGain) {
            speedGain = absoluteMaxSpeedGain;
        } else if (maxSpeedGain >= initialDuration) {
            // already at 1 tick: only whole parallels count
            speedGain = Math.floor(maxSpeedGain / initialDuration) * initialDuration;
        } else {
            speedGain = maxSpeedGain;
        }

        // the fast overclocks are used first
        double effectiveOCs = fast > 0 && speedGain <= fastSpeedGain ?
                Math.log(speedGain) / Math.log(fastSpeedFactor) :
                fast + Math.log(speedGain / fastSpeedGain) / Math.log(speedFactor);
        double eutMultiplier = Math.pow(voltageFactor, effectiveOCs);

        double durationMultiplier;
        double parallel;
        if (speedGain <= initialDuration + 1e-7) {
            durationMultiplier = 1.0 / speedGain;
            parallel = 1;
        } else {
            durationMultiplier = 1.0 / initialDuration;
            parallel = speedGain / initialDuration;
        }

        return new OverclockingLogic.OCResult(eutMultiplier, durationMultiplier,
                (int) Math.ceil(effectiveOCs - 1e-7), (int) Math.round(parallel));
    }
}
