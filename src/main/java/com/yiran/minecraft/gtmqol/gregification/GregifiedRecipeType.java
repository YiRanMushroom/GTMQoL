package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A GT recipe type that proxies recipe types of another mod. After each reload, every recipe of those types goes
 * through {@link #toGTRecipes}, which may give any number of GT recipes (GTCEu's own proxy handling takes exactly
 * one, so {@code RecipeManagerHandlerMixin} does the adding for these types), and the results go to the recipe lookup
 * and EMI.
 */
public class GregifiedRecipeType extends GTRecipeType {

    private final @Nullable ForeignRecipeConverter converter;
    private final Supplier<List<RecipeType<?>>> proxies;

    /** Without proxies, all recipes come from custom recipe logic. */
    public GregifiedRecipeType(ResourceLocation id, Properties properties, @Nullable ForeignRecipeConverter converter,
                               Supplier<List<RecipeType<?>>> proxies) {
        super(id, properties);
        this.converter = converter;
        this.proxies = proxies;
    }

    /**
     * Adds the proxied types to {@link #getProxyRecipes()}, at common setup. Not in the properties: a foreign type
     * may not exist yet when this one is declared (Mekanism creates its recipe types when they are registered).
     * Recipes are only converted on reloads, which come later.
     */
    public void resolveProxy() {
        for (var proxy : proxies.get()) getProxyRecipes().computeIfAbsent(proxy, type -> new ArrayList<>());
    }

    /** Before {@link #toGTRecipes}, with every recipe of one proxied type; see {@link ForeignRecipeConverter#prepare}. */
    public void prepare(List<RecipeHolder<?>> recipes) {
        if (converter == null) return;
        try {
            converter.prepare(recipes);
        } catch (RuntimeException e) {
            GTMQoL.LOGGER.error("Failed to prepare the recipes of {}", this, e);
        }
    }

    /** Every GT recipe for one foreign recipe; empty if the converter leaves it out or fails. */
    public List<RecipeHolder<GTRecipe>> toGTRecipes(RecipeHolder<?> holder) {
        List<RecipeHolder<GTRecipe>> recipes = new ArrayList<>();
        if (converter == null) return recipes;
        try {
            converter.convert(holder, recipe -> {
                var builder = recipeBuilder(holder.id());
                if (!recipe.test(builder)) return;
                GTRecipe built = builder.build();
                // foreign recipe ids are unique in the recipe manager, and with our type's path in front they stay
                // unique across our types; the index tells apart the recipes made from one foreign recipe.
                // Not in the recipe manager: the leading '/' marks it synthetic for EMI.
                ResourceLocation id = holder.id();
                String suffix = recipes.isEmpty() ? "" : "/" + recipes.size();
                built.setId(GTMQoL.id("/gregification/" + this.id.getPath() + "/" + id.getNamespace() + "/" +
                        id.getPath() + suffix));
                recipes.add(new RecipeHolder<>(built.id, built));
            });
        } catch (RuntimeException e) {
            GTMQoL.LOGGER.error("Failed to convert {} to {}", holder.id(), this, e);
            return List.of();
        }
        return recipes;
    }

    /** Only the first; everything of ours goes through {@link #toGTRecipes}. */
    @Override
    public @Nullable RecipeHolder<GTRecipe> toGTRecipe(RecipeHolder<?> holder) {
        var recipes = toGTRecipes(holder);
        return recipes.isEmpty() ? null : recipes.getFirst();
    }
}
