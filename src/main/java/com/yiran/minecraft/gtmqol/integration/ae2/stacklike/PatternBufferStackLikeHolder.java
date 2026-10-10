package com.yiran.minecraft.gtmqol.integration.ae2.stacklike;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.IRecipeHandlerTrait;

import org.jetbrains.annotations.Nullable;

/**
 * Added to gtceu's pattern buffer by {@code mixin.ae2.MEPatternBufferStackLikeMixin}, for the proxies. See
 * {@link PatternBufferStackLike}.
 */
public interface PatternBufferStackLikeHolder {

    /** The handler over every slot's stacks of the capability (like gtceu's aggregate item handler). */
    @Nullable
    IRecipeHandlerTrait<?> gtmqol$getStackLikeAggregate(RecipeCapability<?> cap);
}
