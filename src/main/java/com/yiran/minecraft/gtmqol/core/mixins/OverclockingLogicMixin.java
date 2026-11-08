package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.yiran.minecraft.gtmqol.common.overclock.Overclocking;

import com.google.common.math.IntMath;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.math.RoundingMode;

/**
 * The factor constants ({@code STD_VOLTAGE_FACTOR} etc.) are implicitly {@code static final} and inlined
 * by javac, so the methods are overwritten instead. Injectors are avoided because interface mixins
 * only reliably support {@link Overwrite}.
 */
@Mixin(value = OverclockingLogic.class, remap = false)
public interface OverclockingLogicMixin {

    /**
     * @author Yiran
     * @reason Swap gtceu's four OC constants for 4x/4x and 2x/4x subtick logics, always allow parallels,
     *         and drop the ULV overclock penalty
     */
    @Overwrite
    default @NotNull ModifierFunction getModifier(MetaMachine machine, GTRecipe recipe, long maxVoltage,
                                                  boolean shouldParallel) {
        long EUt = RecipeHelper.getRealEUt(recipe).getTotalEU();
        if (EUt == 0) return ModifierFunction.IDENTITY;

        int OCs = GTUtil.getOCTierByVoltage(maxVoltage) - GTUtil.getTierByVoltage(EUt);
        if (OCs == 0) return ModifierFunction.IDENTITY;

        OverclockingLogic logic = Overclocking.replace((OverclockingLogic) (Object) this);
        int maxParallels;
        if (!shouldParallel) {
            maxParallels = 1;
        } else if (logic == Overclocking.PERFECT) {
            int effectiveBoostPower = OCs * 2 - IntMath.log2(Math.max(1, recipe.duration), RoundingMode.FLOOR) + 4;
            int limit = effectiveBoostPower > 30 ? 2_000_000_000 : (1 << effectiveBoostPower) + 1;
            maxParallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, limit);
        } else if (logic == Overclocking.STANDARD) {
            maxParallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe,
                    Math.max((int) Math.min(maxVoltage / recipe.duration, 2_000_000_000), 1));
        } else {
            maxParallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, 2_000_000_000);
        }

        var params = new OverclockingLogic.OCParams(EUt, recipe.duration, OCs, maxParallels);
        var result = logic.runOverclockingLogic(params, maxVoltage);
        return result.toModifier();
    }

    /**
     * @author Yiran
     * @reason Equivalent overclocking: fractional OC levels, speed gain rounded down to whole parallels
     */
    @Overwrite
    static OverclockingLogic.OCResult subTickParallelOC(OverclockingLogic.OCParams params, long maxVoltage,
                                                        double durationFactor, double voltageFactor) {
        return Overclocking.equivalentOC(params, voltageFactor, 1.0 / durationFactor, 0, 1.0 / durationFactor);
    }

    /**
     * @author Yiran
     * @reason Heating coils as in the 4x/4x overhaul: every OC costs 4x EU/t, and for each 900K the coils are above
     *         the recipe's temperature one OC gives 8x speed instead of 4x (gtceu: one perfect OC per 900K). Those
     *         come first. Equivalent OC, so a recipe whose inputs allow few parallels still reaches 1 tick. gtceu's
     *         EU/t discount of 5% per 900K is applied by the recipe modifier, outside this.
     */
    // Untested: coils 900K over the recipe give one 8x OC and the rest 4x (1800K: two); still 1 tick with few parallels
    @Overwrite
    static OverclockingLogic.OCResult heatingCoilOC(OverclockingLogic.OCParams params, long maxVoltage,
                                                    int recipeTemp, int machineTemp) {
        return Overclocking.equivalentOC(params, 4.0, 4.0, (machineTemp - recipeTemp) / 900, 8.0);
    }
}
