package com.yiran.minecraft.gtmqol.mixin.recipedb;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** {@code RecipeDB.SearchFrame} is private; cast the frame through {@code Object} to read its index. */
@Mixin(targets = "com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB$SearchFrame", remap = false)
public interface SearchFrameAccessor {

    @Accessor("index")
    int gtmqol$getIndex();
}
