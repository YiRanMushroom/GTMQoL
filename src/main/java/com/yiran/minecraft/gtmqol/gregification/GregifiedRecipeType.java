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
 * A GT recipe type that proxies one recipe type of another mod. After each reload, every recipe of that type goes
 * through {@link #toGTRecipes}, which may give any number of GT recipes (GTCEu's own proxy handling takes exactly
 * one, so {@code RecipeManagerHandlerMixin} does the adding for these types), and the results go to the recipe lookup
 * and EMI.
 */
public class GregifiedRecipeType extends GTRecipeType {

    private final ForeignRecipeConverter converter;
    private final Supplier<? extends RecipeType<?>> proxy;

    public GregifiedRecipeType(ResourceLocation id, Properties properties, ForeignRecipeConverter converter,
                               Supplier<? extends RecipeType<?>> proxy) {
        super(id, properties);
        this.converter = converter;
        this.proxy = proxy;
    }

    /**
     * Adds the proxied type to {@link #getProxyRecipes()}, at common setup. Not in the properties: the foreign type
     * may not exist yet when this one is declared (Mekanism creates its recipe types when they are registered).
     * Recipes are only converted on reloads, which come later.
     */
    public void resolveProxy() {
        getProxyRecipes().computeIfAbsent(proxy.get(), type -> new ArrayList<>());
    }

    /** Every GT recipe for one foreign recipe; empty if the converter leaves it out or fails. */
    public List<RecipeHolder<GTRecipe>> toGTRecipes(RecipeHolder<?> holder) {
        List<RecipeHolder<GTRecipe>> recipes = new ArrayList<>();
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
