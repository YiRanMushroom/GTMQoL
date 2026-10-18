package com.yiran.minecraft.gtmqol.common.modifier;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

/**
 * Runs up to {@code max} recipes at once at the same EU/t and duration, whatever the voltage: like Create's fans,
 * which process a whole stack as fast as one item. Meant as the last modifier, after a non-subtick overclock
 * ({@code OC_NON_PERFECT} / {@code OC_PERFECT}); subtick parallels would be multiplied again.
 */
public final class AbsoluteParallel {

    private AbsoluteParallel() {}

    public static RecipeModifier upTo(int max) {
        return (machine, recipe) -> {
            int parallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, max);
            if (parallels == 0) return ModifierFunction.NULL;
            if (parallels == 1) return ModifierFunction.IDENTITY;
            // tick modifiers leave EU/t alone, so this scales everything but the energy
            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallels))
                    .parallels(parallels)
                    .build();
        };
    }
}
