package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The marks of a {@link GhostIngredientHatchPartMachine} ({@link GhostIngredientList}). Provides them to recipes as
 * {@link GhostIngredientRecipeCapability} inputs and never consumes them.
 */
public class GhostIngredientHandler extends NotifiableRecipeHandlerTrait<GhostIngredient<?>> {

    @SaveField
    public final GhostIngredientList marks = new GhostIngredientList();

    public GhostIngredientHandler() {
        // markAsChanged: the block entity is only saved again when marked as changed
        marks.setOnChanged(() -> {
            markAsChanged();
            notifyListeners();
        });
    }

    @Override
    public @NotNull List<GhostIngredient<?>> handleRecipeInner(IO io, @Nullable GTRecipe recipe,
                                                                  List<GhostIngredient<?>> left,
                                                                  boolean simulate) {
        if (io != IO.IN) return left;
        left.removeIf(marks::contains);
        return left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return new ArrayList<>(new LinkedHashSet<>(marks.view()));
    }

    @Override
    public double getTotalContentAmount() {
        return marks.size();
    }

    @Override
    public RecipeCapability<GhostIngredient<?>> getCapability() {
        return GhostIngredientRecipeCapability.CAP;
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }
}
