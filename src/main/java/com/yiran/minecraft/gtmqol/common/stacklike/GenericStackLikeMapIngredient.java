package com.yiran.minecraft.gtmqol.common.stacklike;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

/**
 * Recipe lookup node of one stack type, see {@link GenericStackLikeType#lookupKey}. Tag ingredients become one
 * node per matched type.
 */
public final class GenericStackLikeMapIngredient extends AbstractMapIngredient {

    private final Object key;

    public GenericStackLikeMapIngredient(Object key) {
        this.key = key;
    }

    @Override
    protected int hash() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && key.equals(((GenericStackLikeMapIngredient) o).key);
    }

    @Override
    public String toString() {
        return "GenericStackLikeMapIngredient{" + key + "}";
    }
}
