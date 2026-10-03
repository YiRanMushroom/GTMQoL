package com.yiran.minecraft.gtmqol.common.overclock;

import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

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

    private Overclocking() {}

    public static OverclockingLogic replace(OverclockingLogic logic) {
        if (logic == OverclockingLogic.NON_PERFECT_OVERCLOCK || logic == OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK) {
            return STANDARD;
        }
        if (logic == OverclockingLogic.PERFECT_OVERCLOCK || logic == OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK) {
            return PERFECT;
        }
        return logic;
    }

    /** Both replacements gain 4x speed per overclock. */
    public static boolean isReplacement(OverclockingLogic logic) {
        return logic == STANDARD || logic == PERFECT;
    }
}
