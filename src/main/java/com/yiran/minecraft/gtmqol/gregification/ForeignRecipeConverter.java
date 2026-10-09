package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Turns one recipe of another mod into a GT recipe.
 */
@FunctionalInterface
public interface ForeignRecipeConverter {

    /**
     * @param holder  a recipe of the proxied type
     * @param builder a fresh builder of the gregified recipe type, with the foreign recipe's id
     * @return false to leave this recipe out
     */
    boolean convert(RecipeHolder<?> holder, GTRecipeBuilder builder);
}
