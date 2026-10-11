package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A GT recipe type that proxies one recipe type of another mod. After each reload, every recipe of that type goes
 * through {@link #toGTRecipes}, which may give any number of GT recipes (GTCEu's own proxy handling takes exactly
 * one, so {@code RecipeManagerHandlerMixin} does the adding for these types), and the results go to the recipe lookup
 * and the recipe viewer.
 */
public class GregifiedRecipeType extends GTRecipeType {

    private final ForeignRecipeConverter converter;
    private final Supplier<? extends RecipeType<?>> proxy;

    public GregifiedRecipeType(ResourceLocation id, String group, ForeignRecipeConverter converter,
                               Supplier<? extends RecipeType<?>> proxy) {
        super(id, group);
        this.converter = converter;
        this.proxy = proxy;
    }

    /**
     * Adds the proxied type to {@link #getProxyRecipes()}, at common setup. Not in the constructor: the foreign type
     * may not exist yet when this one is declared (Mekanism's recipe types are deferred registry objects). Recipes are
     * only converted on reloads, which come later.
     */
    public void resolveProxy() {
        getProxyRecipes().computeIfAbsent(proxy.get(), type -> new ArrayList<>());
    }

    /** Every GT recipe for one foreign recipe; empty if the converter leaves it out or fails. */
    public List<GTRecipe> toGTRecipes(Recipe<?> foreign) {
        List<GTRecipe> recipes = new ArrayList<>();
        ResourceLocation id = foreign.getId();
        try {
            converter.convert(foreign, recipe -> {
                var builder = recipeBuilder(id);
                if (!recipe.test(builder)) return;
                // the builder's own GTRecipe, without GTCEu's JSON round trip in toGTrecipe (which also needs every
                // content to be JSON serializable)
                GTRecipe built = builder.buildRawRecipe();
                // foreign recipe ids are unique in the recipe manager, and with our type's path in front they stay
                // unique across our types; the index tells apart the recipes made from one foreign recipe.
                // Not in the recipe manager: the leading '/' marks it synthetic for EMI.
                String suffix = recipes.isEmpty() ? "" : "/" + recipes.size();
                built.id = GTMQoL.id("/gregification/" + registryName.getPath() + "/" + id.getNamespace() + "/" +
                        id.getPath() + suffix);
                recipes.add(built);
            });
        } catch (RuntimeException e) {
            GTMQoL.LOGGER.error("Failed to convert {} to {}", id, this, e);
            return List.of();
        }
        return recipes;
    }

    /** Only the first; everything of ours goes through {@link #toGTRecipes}. */
    @Override
    public @Nullable GTRecipe toGTrecipe(ResourceLocation id, Recipe<?> recipe) {
        var recipes = toGTRecipes(recipe);
        return recipes.isEmpty() ? null : recipes.get(0);
    }
}
