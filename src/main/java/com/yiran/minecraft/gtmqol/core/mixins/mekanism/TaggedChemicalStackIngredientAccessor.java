package com.yiran.minecraft.gtmqol.core.mixins.mekanism;

import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraftforge.registries.tags.ITag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mekanism 10.4 has no getters for the tag and the amount of a tag ingredient. {@code ChemicalStackLike} needs them
 * while recipes load, when the tag's contents aren't known yet ({@code withAmount}, {@code ingredientAmount},
 * {@code lookupKeys}).
 */
@Mixin(value = TaggedChemicalStackIngredient.class, remap = false)
public interface TaggedChemicalStackIngredientAccessor {

    @Accessor("tag")
    ITag<?> gtmqol$getTag();

    @Accessor("amount")
    long gtmqol$getAmount();
}
