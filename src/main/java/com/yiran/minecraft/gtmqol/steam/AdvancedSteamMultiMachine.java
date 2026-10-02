package com.yiran.minecraft.gtmqol.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;

import org.jetbrains.annotations.Nullable;

/**
 * Base of the advanced steam multiblocks. Recipes up to {@link #maxRecipeTier} (MV unless the machine says
 * otherwise) are overclocked as if the machine were that tier (non-perfect: EU/t ×4, duration ÷2), then run
 * at {@link #DURATION_MULTIPLIER} of the time and {@link #EUT_MULTIPLIER} of the EU/t. 16 parallels, or
 * whatever a {@link SteamParallelHatchPartMachine} is set to.
 */
public class AdvancedSteamMultiMachine extends SteamParallelMultiblockMachine {

    public static final int DEFAULT_PARALLELS = 16;
    public static final double DURATION_MULTIPLIER = 0.8;
    public static final double EUT_MULTIPLIER = 0.75;

    private final int maxRecipeTier;

    @Nullable
    private SteamParallelHatchPartMachine parallelHatch;

    public AdvancedSteamMultiMachine(BlockEntityCreationInfo info, int maxRecipeTier) {
        super(info, DEFAULT_PARALLELS);
        this.maxRecipeTier = maxRecipeTier;
    }

    public AdvancedSteamMultiMachine(BlockEntityCreationInfo info) {
        this(info, GTValues.MV);
    }

    public int getMaxRecipeTier() {
        return maxRecipeTier;
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof SteamParallelHatchPartMachine hatch) {
                parallelHatch = hatch;
                break;
            }
        }
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        parallelHatch = null;
    }

    @Override
    public int getMaxParallels() {
        return parallelHatch != null ? parallelHatch.getCurrentParallel() : DEFAULT_PARALLELS;
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof AdvancedSteamMultiMachine steamMachine)) {
            return RecipeModifier.nullWrongType(AdvancedSteamMultiMachine.class, machine);
        }
        int maxTier = steamMachine.getMaxRecipeTier();
        int tier = RecipeHelper.getRecipeEUtTier(recipe);
        if (tier > maxTier) return ModifierFunction.NULL;

        // Like GTCEu's OverclockingLogic, a ULV recipe gets one overclock less.
        double eutMultiplier = 1;
        double durationMultiplier = 1;
        for (int t = Math.max(tier, GTValues.LV); t < maxTier && recipe.duration * durationMultiplier >= 2; t++) {
            eutMultiplier *= 4;
            durationMultiplier /= 2;
        }

        int parallels = ParallelLogic.getParallelAmount(machine, recipe, steamMachine.getMaxParallels());
        if (parallels == 0) return ModifierFunction.NULL;
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallels))
                .outputModifier(ContentModifier.multiplier(parallels))
                .durationMultiplier(durationMultiplier * DURATION_MULTIPLIER)
                .eutMultiplier(eutMultiplier * EUT_MULTIPLIER * parallels)
                .parallels(parallels)
                .build();
    }
}
