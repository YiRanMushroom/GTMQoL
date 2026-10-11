package com.yiran.minecraft.gtmqol.common.stacklike;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;

import java.util.List;

/**
 * Describes a fluid-like stack type (a resource with a type and a long amount, e.g. Mekanism chemicals) to
 * {@link GenericStackLikeRecipeCapability}. {@code S} is the stack, {@code I} the recipe ingredient (an ingredient
 * with an amount, may match several types, e.g. a tag). Ingredients are treated as immutable.
 */
public interface GenericStackLikeType<S, I> {

    /** Also the recipe capability's id. */
    ResourceLocation id();

    Class<S> stackClass();

    Class<I> ingredientClass();

    // Stacks

    boolean isEmpty(S stack);

    long stackAmount(S stack);

    S copyWithAmount(S stack, long amount);

    /** Same type, amounts are ignored. */
    boolean isSameType(S a, S b);

    /**
     * Equal (and same hash) for stacks of the same type, amounts are ignored. Used for recipe lookup.
     */
    Object lookupKey(S stack);

    Codec<S> stackCodec();

    /** The type's name, without the amount. Shown in recipe viewers and Jade. */
    Component displayName(S stack);

    // Ingredients

    long ingredientAmount(I ingredient);

    I withAmount(I ingredient, long amount);

    /** Whether the ingredient matches the stack's type, the amount is ignored. */
    boolean test(I ingredient, S stack);

    /** Every stack the ingredient matches, with the ingredient's amount. Empty if it matches nothing. */
    List<S> getStacks(I ingredient);

    /** Ingredient matching exactly this stack's type and amount. */
    I of(S stack);

    Codec<I> ingredientCodec();

    void writeIngredient(FriendlyByteBuf buf, I ingredient);

    I readIngredient(FriendlyByteBuf buf);
}
