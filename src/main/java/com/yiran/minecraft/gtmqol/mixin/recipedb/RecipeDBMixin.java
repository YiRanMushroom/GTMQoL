package com.yiran.minecraft.gtmqol.mixin.recipedb;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.yiran.minecraft.gtmqol.recipedb.GroupedIngredientList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(value = RecipeDB.class, remap = false)
public class RecipeDBMixin {

    /**
     * @author yiran
     * @reason Walk the holder's recipe handler lists instead of the flattened handlers, so every ingredient
     *         remembers which group it came from. See docs/RECIPEDB_REFACTOR.md.
     */
    @Overwrite
    private @Nullable List<List<AbstractMapIngredient>> fromHolder(@NotNull IRecipeCapabilityHolder holder) {
        GroupedIngredientList list = GroupedIngredientList.fromHolder(holder);
        return list.isEmpty() ? null : list;
    }
}
