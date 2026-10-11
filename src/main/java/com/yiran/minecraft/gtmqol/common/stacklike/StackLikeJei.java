package com.yiran.minecraft.gtmqol.common.stacklike;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.List;

/**
 * {@link GenericStackLikeRecipeCapability} contents in gtceu's JEI recipe ingredients (the
 * {@code GTRecipeJEICategoryStackLikeMixin}), which JEI uses for recipe lookup. gtceu only lists items and fluids.
 * The displayed slots are separate (ModularUI's, see {@link StackLikeRecipeViewer}). JEI has to know the stack class
 * as an ingredient type.
 */
public final class StackLikeJei {

    private StackLikeJei() {}

    public static void addIngredients(IRecipeLayoutBuilder builder, GTRecipe recipe) {
        for (var cap : GenericStackLikeRecipeCapability.ALL) {
            add(builder, cap, recipe.getInputContents(cap), RecipeIngredientRole.INPUT);
            add(builder, cap, recipe.getOutputContents(cap), RecipeIngredientRole.OUTPUT);
        }
    }

    private static <S, I> void add(IRecipeLayoutBuilder builder, GenericStackLikeRecipeCapability<S, I> cap,
                                   List<Content> contents, RecipeIngredientRole role) {
        for (Content content : contents) {
            List<S> stacks = cap.type.getStacks(cap.of(content.content()));
            if (!stacks.isEmpty()) builder.addSlot(role).addIngredientsUnsafe(stacks);
        }
    }
}
