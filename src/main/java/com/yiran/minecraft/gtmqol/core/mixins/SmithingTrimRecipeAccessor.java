package com.yiran.minecraft.gtmqol.core.mixins;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Smithing recipes hide their ingredients; CraftingPattern.grid lays them out for the recipe viewers' transfer. */
@Mixin(SmithingTrimRecipe.class)
public interface SmithingTrimRecipeAccessor {

    @Accessor("template")
    Ingredient gtmqol$template();

    @Accessor("base")
    Ingredient gtmqol$base();

    @Accessor("addition")
    Ingredient gtmqol$addition();
}
