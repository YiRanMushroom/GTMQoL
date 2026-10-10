package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * A GT recipe type that proxies one recipe type of another mod: GTCEu's {@code RecipeManagerLateMixin} hands every
 * recipe of that type to {@link #toGTRecipe} after each reload, and the results go to the recipe lookup and EMI.
 *
 * <p>Returns null for recipes the converter leaves out; {@code RecipeManagerHandlerMixin} skips those.</p>
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

    @Override
    public @Nullable RecipeHolder<GTRecipe> toGTRecipe(RecipeHolder<?> holder) {
        var builder = recipeBuilder(holder.id());
        try {
            if (!converter.convert(holder, builder)) return null;
        } catch (RuntimeException e) {
            GTMQoL.LOGGER.error("Failed to convert {} to {}", holder.id(), this, e);
            return null;
        }
        GTRecipe built = builder.build();
        // foreign recipe ids are unique in the recipe manager, and with our type's path in front they stay unique
        // when two of our types proxy the same foreign one (e.g. both directions of Mekanism's rotary recipes).
        // Not in the recipe manager: the leading '/' marks it synthetic for EMI.
        ResourceLocation id = holder.id();
        built.setId(GTMQoL.id("/gregification/" + this.id.getPath() + "/" + id.getNamespace() + "/" + id.getPath()));
        return new RecipeHolder<>(built.id, built);
    }
}
