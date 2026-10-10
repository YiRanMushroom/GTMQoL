package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.function.Predicate;

/**
 * Turns one recipe of another mod into any number of GT recipes, e.g. both directions of a Mekanism rotary recipe.
 */
@FunctionalInterface
public interface ForeignRecipeConverter {

    /**
     * @param holder a recipe of the proxied type
     * @param output takes each GT recipe; adding none leaves the foreign recipe out
     */
    void convert(RecipeHolder<?> holder, Output output);

    interface Output {

        /**
         * One GT recipe: {@code recipe} fills a fresh builder of the gregified recipe type, or returns false to
         * drop it.
         */
        void add(Predicate<GTRecipeBuilder> recipe);
    }

    /** One GT recipe per foreign recipe. */
    @FunctionalInterface
    interface Single {

        /**
         * @param holder  a recipe of the proxied type
         * @param builder a fresh builder of the gregified recipe type, with the foreign recipe's id
         * @return false to leave this recipe out
         */
        boolean convert(RecipeHolder<?> holder, GTRecipeBuilder builder);
    }

    static ForeignRecipeConverter single(Single converter) {
        return (holder, output) -> output.add(builder -> converter.convert(holder, builder));
    }
}
