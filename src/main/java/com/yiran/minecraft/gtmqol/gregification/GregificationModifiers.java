package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.config.ConfigHolder;

import org.jetbrains.annotations.NotNull;

/**
 * Recipe logic of gregified machines, single blocks and multiblocks alike.
 */
public final class GregificationModifiers {

    /**
     * Uses all of the machine's voltage at the same energy per recipe: {@code s = voltage / EUt} times as fast, so EU/t
     * goes up by as much as the duration goes down. Below 1 tick it runs {@code s / duration} subtick parallels.
     * Only rounding to whole ticks loses a little speed.
     */
    public static final RecipeModifier OVERCLOCK = GregificationModifiers::overclock;

    /**
     * GTCEu's batch mode; single blocks toggle it with their {@link BatchModeTrait}.
     */
    public static final RecipeModifier BATCH = GregificationModifiers::batch;

    private GregificationModifiers() {}

    private static @NotNull ModifierFunction overclock(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof IOverclockMachine overclockMachine)) return ModifierFunction.IDENTITY;
        long eut = RecipeHelper.getRealEUt(recipe).getTotalEU();
        if (eut <= 0) return ModifierFunction.IDENTITY;
        long voltage = overclockMachine.getOverclockVoltage();
        if (eut > voltage) return ModifierFunction.NULL;

        double speed = (double) voltage / eut;
        int duration = recipe.duration;

        if (speed <= duration) {
            int newDuration = Math.max(1, (int) Math.ceil(duration / speed));
            if (newDuration >= duration) return ModifierFunction.IDENTITY;
            return ModifierFunction.builder()
                    // + 0.5: ContentModifier truncates, and newDuration / duration * duration can come out just below
                    .durationMultiplier((newDuration + 0.5) / duration)
                    .eutMultiplier((double) duration / newDuration)
                    .build();
        }

        int parallels = (int) Math.min(Integer.MAX_VALUE, (long) (speed / duration));
        parallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, parallels);
        if (parallels == 0) return ModifierFunction.NULL;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .durationMultiplier(1.5 / duration)
                .eutMultiplier((double) duration * parallels)
                .subtickParallels(parallels)
                .build();
    }

    private static @NotNull ModifierFunction batch(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!batchEnabled(machine)) return ModifierFunction.IDENTITY;
        int batchDuration = ConfigHolder.INSTANCE.machines.batchDuration;
        if (recipe.duration >= batchDuration) return ModifierFunction.IDENTITY;

        int parallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, batchDuration / recipe.duration);
        if (parallels == 0) return ModifierFunction.NULL;
        if (parallels == 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallels))
                .outputModifier(ContentModifier.multiplier(parallels))
                .durationMultiplier(parallels)
                .batchParallels(parallels)
                .build();
    }

    private static boolean batchEnabled(MetaMachine machine) {
        if (machine instanceof MultiblockControllerMachine controller) {
            return controller.isFormed() && controller.isBatchEnabled();
        }
        BatchModeTrait trait = machine.getTrait(BatchModeTrait.class);
        return trait != null && trait.isBatchEnabled();
    }
}
