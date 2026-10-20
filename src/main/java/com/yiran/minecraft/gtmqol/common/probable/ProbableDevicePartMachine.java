package com.yiran.minecraft.gtmqol.common.probable;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A multiblock part that changes every recipe its machine runs, through gtceu's part {@code modifyRecipe} hook (after
 * the machine's own modifiers, so on the parallel recipe). Certainty: chanced outputs always come out, ranged outputs
 * at their largest amount. Improbability: chanced inputs are never consumed, ranged inputs at their smallest amount.
 * Port of the 2.0.0 (GTCEu 7.x) devices, without their per-recipe-id cache: one id now covers every parallel count.
 */
public class ProbableDevicePartMachine extends MultiblockPartMachine {

    private final boolean certainty;

    public ProbableDevicePartMachine(BlockEntityCreationInfo info, boolean certainty) {
        super(info);
        this.certainty = certainty;
    }

    @Override
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
        GTRecipe modified = recipe.copy();
        if (certainty) {
            certain(modified.outputs, modified.outputChanceLogics);
            certain(modified.tickOutputs, modified.tickOutputChanceLogics);
        } else {
            improbable(modified.inputs, modified.inputChanceLogics);
            improbable(modified.tickInputs, modified.tickInputChanceLogics);
        }
        return modified;
    }

    private static void certain(Map<RecipeCapability<?>, List<Content>> contents,
                                Map<RecipeCapability<?>, ChanceLogic> logics) {
        int max = ChanceLogic.getMaxChancedValue();
        contents.replaceAll((cap, list) -> list.stream()
                .map(content -> {
                    Object fixed = fixRange(content.content(), true);
                    return new Content(fixed == null ? content.content() : fixed, max, max);
                })
                .toList());
        // everything is certain now, so XOR / FIRST would drop outputs that are guaranteed
        logics.replaceAll((cap, logic) -> ChanceLogic.OR);
    }

    private static void improbable(Map<RecipeCapability<?>, List<Content>> contents,
                                   Map<RecipeCapability<?>, ChanceLogic> logics) {
        contents.replaceAll((cap, list) -> list.stream()
                .map(content -> {
                    Object fixed = fixRange(content.content(), false);
                    // a range starting at 0 may consume nothing: the same as never consumed
                    if (content.isChanced() || fixed == null) {
                        return new Content(fixed == null ? content.content() : fixed, 0, content.maxChance());
                    }
                    return new Content(fixed, content.chance(), content.maxChance());
                })
                .toList());
        logics.replaceAll((cap, logic) -> ChanceLogic.OR);
    }

    /** A ranged amount fixed to its largest or smallest value; null if that is 0. Other contents as they are. */
    private static @Nullable Object fixRange(Object content, boolean largest) {
        if (content instanceof SizedIngredient sized && sized.getInner() instanceof IntProviderIngredient ranged) {
            return fixRange(ranged, largest);
        }
        if (content instanceof IntProviderIngredient ranged) return fixRange(ranged, largest);
        if (content instanceof IntProviderFluidIngredient ranged) {
            int amount = largest ? ranged.getCountProvider().getMaxValue() : ranged.getCountProvider().getMinValue();
            if (amount <= 0) return null;
            FluidIngredient fluid = ranged.getInner().copy();
            fluid.setAmount(amount);
            return fluid;
        }
        return content;
    }

    private static @Nullable Object fixRange(IntProviderIngredient ranged, boolean largest) {
        int count = largest ? ranged.getCountProvider().getMaxValue() : ranged.getCountProvider().getMinValue();
        return count > 0 ? SizedIngredient.create(ranged.getInner(), count) : null;
    }
}
