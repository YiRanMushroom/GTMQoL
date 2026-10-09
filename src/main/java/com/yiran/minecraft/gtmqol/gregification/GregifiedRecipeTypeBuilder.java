package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.GTRecipeTypeBuilder;

import net.minecraft.world.item.crafting.RecipeType;

import com.tterrag.registrate.builders.BuilderCallback;

/**
 * {@link GTRecipeTypeBuilder} that creates a {@link GregifiedRecipeType}.
 */
public class GregifiedRecipeTypeBuilder extends GTRecipeTypeBuilder {

    private final ForeignRecipeConverter converter;

    public GregifiedRecipeTypeBuilder(GTRegistrate owner, String name, BuilderCallback callback, String group,
                                      ForeignRecipeConverter converter, RecipeType<?> proxy) {
        super(owner, name, callback, group, proxy);
        this.converter = converter;
    }

    @Override
    protected GTRecipeType createEntry() {
        return new GregifiedRecipeType(getOwner().makeResourceLocation(getName()), getProperties(), converter);
    }
}
