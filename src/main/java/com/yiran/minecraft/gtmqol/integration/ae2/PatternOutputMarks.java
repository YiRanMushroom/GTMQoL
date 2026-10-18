package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.yiran.minecraft.gtmqol.common.ghost.FluidGhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.common.ghost.ItemGhostIngredient;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Ghost ingredient marks for the main outputs of pattern buffer patterns ({@link AbstractMEPatternBufferPartMachine},
 * when its toggle is on): each slot marks the output of the pattern it is working on, so it picks the recipe making
 * it. Never consumed.
 */
public final class PatternOutputMarks implements IRecipeHandler<GhostIngredient<?>> {

    public static final String TOGGLE_KEY = "gtmqol.gui.pattern_buffer.pattern_output_marks";

    private final Supplier<List<GhostIngredient<?>>> marks;

    public PatternOutputMarks(Supplier<List<GhostIngredient<?>>> marks) {
        this.marks = marks;
    }

    /** The mark for a pattern's main output, or null for none. */
    public static @Nullable GhostIngredient<?> of(@Nullable IPatternDetails pattern) {
        if (pattern == null) return null;
        GenericStack output = pattern.getPrimaryOutput();
        if (output == null) return null;
        if (output.what() instanceof AEItemKey item) return new ItemGhostIngredient(item.toStack());
        if (output.what() instanceof AEFluidKey fluid) return new FluidGhostIngredient(fluid.toStack(1));
        return null;
    }

    @Override
    public List<GhostIngredient<?>> handleRecipeInner(IO io, @Nullable GTRecipe recipe, List<GhostIngredient<?>> left,
                                                     boolean simulate) {
        if (io != IO.IN) return left;
        List<GhostIngredient<?>> current = marks.get();
        left.removeIf(current::contains);
        return left;
    }

    @Override
    public List<Object> getContents() {
        return new ArrayList<>(marks.get());
    }

    @Override
    public double getTotalContentAmount() {
        return marks.get().size();
    }

    @Override
    public RecipeCapability<GhostIngredient<?>> getCapability() {
        return GhostIngredientRecipeCapability.CAP;
    }
}
