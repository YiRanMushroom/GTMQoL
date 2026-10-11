package com.yiran.minecraft.gtmqol.core.mixins.mekanism;

import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraftforge.registries.tags.ITag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Mekanism 10.4 has no getter for the tag of a tag ingredient; {@code ChemicalStackLike.withAmount} needs it. */
@Mixin(value = TaggedChemicalStackIngredient.class, remap = false)
public interface TaggedChemicalStackIngredientAccessor {

    @Accessor("tag")
    ITag<?> gtmqol$getTag();
}
