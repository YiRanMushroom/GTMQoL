package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import org.jetbrains.annotations.Nullable;

/**
 * A GT recipe type that proxies one recipe type of another mod: GTCEu's {@code RecipeManagerLateMixin} hands every
 * recipe of that type to {@link #toGTRecipe} after each reload, and the results go to the recipe lookup and EMI.
 *
 * <p>Returns null for recipes the converter leaves out; {@code RecipeManagerHandlerMixin} skips those.</p>
 */
public class GregifiedRecipeType extends GTRecipeType {

    private final ForeignRecipeConverter converter;

    public GregifiedRecipeType(ResourceLocation id, Properties properties, ForeignRecipeConverter converter) {
        super(id, properties);
        this.converter = converter;
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
        // foreign recipe ids are unique in the recipe manager, so they stay unique under our namespace.
        // Not in the recipe manager: the leading '/' marks it synthetic for EMI.
        ResourceLocation id = holder.id();
        built.setId(GTMQoL.id("/gregification/" + id.getNamespace() + "/" + id.getPath()));
        return new RecipeHolder<>(built.id, built);
    }
}
