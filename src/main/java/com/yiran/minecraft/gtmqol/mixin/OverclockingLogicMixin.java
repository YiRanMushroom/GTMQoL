package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.yiran.minecraft.gtmqol.overclock.Overclocking;

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
        if (OCs <= 0) return ModifierFunction.IDENTITY;

        OverclockingLogic logic = Overclocking.replace((OverclockingLogic) (Object) this);
        int maxParallels;
        if (!shouldParallel) {
            maxParallels = 1;
        } else if (Overclocking.isReplacement(logic)) {
            // 4x speed per OC: up to 4^OCs / duration parallels once at 1 tick, with x16 headroom
            int power = Math.max(0, OCs * 2 - IntMath.log2(Math.max(1, recipe.duration), RoundingMode.FLOOR) + 4);
            int limit = power > 30 ? 2_000_000_000 : (1 << power) + 1;
            maxParallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, limit);
        } else {
            maxParallels = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, 2_000_000_000);
        }

        var params = new OverclockingLogic.OCParams(EUt, recipe.duration, OCs, maxParallels);
        return logic.runOverclockingLogic(params, maxVoltage).toModifier();
    }

    /**
     * @author Yiran
     * @reason Equivalent overclocking: fractional OC levels, speed gain rounded down to whole parallels
     */
    @Overwrite
    static OverclockingLogic.OCResult subTickParallelOC(OverclockingLogic.OCParams params, long maxVoltage,
                                                        double durationFactor, double voltageFactor) {
        double initialDuration = params.duration();
        int ocAmount = params.ocAmount();
        int maxParallels = params.maxParallels();

        if (maxParallels < 0) return new OverclockingLogic.OCResult(1, 1, 0, 1);
        if (maxParallels == 0) maxParallels = 1;

        double speedGainFactor = 1.0 / durationFactor;
        double maxSpeedGain = Math.pow(speedGainFactor, ocAmount);
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

        double effectiveOCs = Math.log(speedGain) / Math.log(speedGainFactor);
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

    /**
     * @author Yiran
     * @reason Every heating coil OC is 4x EU/t for 8x speed
     */
    @Overwrite
    static OverclockingLogic.OCResult heatingCoilOC(OverclockingLogic.OCParams params, long maxVoltage,
                                                    int recipeTemp, int machineTemp) {
        double duration = params.duration();
        double eut = params.eut();
        int ocAmount = params.ocAmount();
        int maxParallels = params.maxParallels();

        double parallel = 1;
        boolean shouldParallel = false;
        int ocLevel = 0;
        double durationMultiplier = 1;

        while (ocAmount-- > 0) {
            double potentialEUt = eut * 4.0;
            if (potentialEUt > maxVoltage) break;

            if (shouldParallel || duration * 0.125 < 1) {
                double potentialParallel = parallel * 8.0;
                if (potentialParallel > maxParallels) break;
                parallel = potentialParallel;
                shouldParallel = true;
            } else {
                duration *= 0.125;
                durationMultiplier *= 0.125;
            }

            eut = potentialEUt;
            ocLevel++;
        }

        return new OverclockingLogic.OCResult(Math.pow(4.0, ocLevel), durationMultiplier, ocLevel, (int) parallel);
    }
}
