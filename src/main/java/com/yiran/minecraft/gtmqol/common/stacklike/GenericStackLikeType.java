package com.yiran.minecraft.gtmqol.common.stacklike;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * The {@link #lookupKey}s of everything the ingredient matches, for GT's recipe lookup. That is built while
     * recipes load, before the registries have the new tags, so a type whose ingredients resolve tags (and keep what
     * they find) works them out from the ingredient's parts and {@code LoadingTags} instead. By default the keys of
     * {@link #getStacks}.
     */
    default Set<Object> lookupKeys(I ingredient) {
        Set<Object> keys = new LinkedHashSet<>();
        for (S stack : getStacks(ingredient)) keys.add(lookupKey(stack));
        return keys;
    }

    /** Ingredient matching exactly this stack's type and amount. */
    I of(S stack);

    Codec<I> ingredientCodec();

    StreamCodec<RegistryFriendlyByteBuf, I> ingredientStreamCodec();
}
