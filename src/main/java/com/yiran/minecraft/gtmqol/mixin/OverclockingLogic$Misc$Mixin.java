package com.yiran.minecraft.gtmqol.mixin;

import com.google.common.math.IntMath;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.math.RoundingMode;

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.NON_PERFECT_OVERCLOCK;
import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.PERFECT_OVERCLOCK;
import static com.mojang.text2speech.Narrator.LOGGER;

@Mixin(OverclockingLogic.class)
public interface OverclockingLogic$Misc$Mixin {

    @Shadow(remap = false)
    OverclockingLogic.OCResult runOverclockingLogic(OverclockingLogic.OCParams params, long maxVoltage);

    /**
     * @author Yiran
     * @reason Always allow sub-tick parallel
     */
    @Overwrite(remap = false)
    default @NotNull ModifierFunction getModifier(MetaMachine machine, GTRecipe recipe, long maxVoltage, boolean shouldParallel) {
        long EUt = RecipeHelper.getRealEUt(recipe).getTotalEU();
        if (EUt == 0L) {
            return ModifierFunction.IDENTITY;
        } else {
            int recipeTier = GTUtil.getTierByVoltage(EUt);
            int maximumTier = GTUtil.getOCTierByVoltage(maxVoltage);
            int OCs = maximumTier - recipeTier;

            if (OCs == 0) {
                return ModifierFunction.IDENTITY;
            } else {
                int maxParallels;
                if (shouldParallel && this != PERFECT_OVERCLOCK && this != NON_PERFECT_OVERCLOCK) {
                    int lg = IntMath.log2(recipe.duration, RoundingMode.FLOOR) / 2;
                    if (lg > OCs) {
                        maxParallels = 16;
                    } else {
                        int p = GTMath.saturatedCast((1L << 2 * (OCs - lg)) + 1L);
                        maxParallels = ParallelLogic.getParallelAmount(machine, recipe, p);
                    }
                } else {
                    maxParallels = 1;
                }

                OverclockingLogic.OCParams params = new OverclockingLogic.OCParams(EUt, recipe.duration, OCs, maxParallels);
                OverclockingLogic.OCResult result = this.runOverclockingLogic(params, maxVoltage);
                return result.toModifier();
            }
        }
    }

    /**
     * @author Yiran
     * @reason change perfect OC amount logic
     */
    @Overwrite(remap = false)
    @NotNull
    static OverclockingLogic.OCResult heatingCoilOC(OverclockingLogic.OCParams params, long maxVoltage, int recipeTemp, int machineTemp) {
        double duration = params.duration();
        double eut = params.eut();
        int ocAmount = params.ocAmount();
        int maxParallels = params.maxParallels();

        // LOGGER.info("Starting heating coil OC calculation with params: " +
        // "EUt={}, duration={}, OC amount={}, max parallels={}, recipeTemp={}, machineTemp={}",
        // eut, duration, ocAmount, maxParallels, recipeTemp, machineTemp);

        double parallel = 1;
        boolean shouldParallel = false;
        int ocLevel = 0;
        double durationMultiplier = 1;

        while (ocAmount-- > 0) {
            // Check if EUt can be multiplied again without going over the max
            double potentialEUt = eut * 4.0;
            if (potentialEUt > maxVoltage) break;

            // If we're already doing parallels or our duration would go below 1, try parallels
            double dFactor = 0.125;
            if (shouldParallel || duration * dFactor < 1) {
                double pFactor = 8.0;
                double potentialParallel = parallel * pFactor;
                if (potentialParallel > maxParallels) break;
                parallel = potentialParallel;
                shouldParallel = true;
            } else {
                duration *= dFactor;
                durationMultiplier *= dFactor;
            }

            // Only set EUt after checking parallels - no need to OC if parallels would be too high
            eut = potentialEUt;
            ocLevel++;
        }

        return new OverclockingLogic.OCResult(Math.pow(4.0, ocLevel), durationMultiplier, ocLevel, (int) parallel);
    }
}
