package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.GTRecipeTypeBuilder;

import net.minecraft.world.item.crafting.RecipeType;

import com.tterrag.registrate.builders.BuilderCallback;

import java.util.function.Supplier;

/**
 * {@link GTRecipeTypeBuilder} that creates a {@link GregifiedRecipeType}. The proxied type is added later, see
 * {@link GregifiedRecipeType#resolveProxy()}.
 */
public class GregifiedRecipeTypeBuilder extends GTRecipeTypeBuilder {

    private final ForeignRecipeConverter converter;
    private final Supplier<? extends RecipeType<?>> proxy;

    public GregifiedRecipeTypeBuilder(GTRegistrate owner, String name, BuilderCallback callback, String group,
                                      ForeignRecipeConverter converter, Supplier<? extends RecipeType<?>> proxy) {
        super(owner, name, callback, group);
        this.converter = converter;
        this.proxy = proxy;
    }

    @Override
    protected GTRecipeType createEntry() {
        return new GregifiedRecipeType(getOwner().makeResourceLocation(getName()), getProperties(), converter, proxy);
    }
}
