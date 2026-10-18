package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

/**
 * Recipe lookup node of a {@link GhostIngredientRecipeCapability} content, equal for ghost ingredients that mark the
 * same thing. Its own class, so a mark never equals a real item or fluid node.
 */
public final class GhostMapIngredient extends AbstractMapIngredient {

    private final GhostIngredient<?> ghost;

    public GhostMapIngredient(GhostIngredient<?> ghost) {
        this.ghost = ghost;
    }

    @Override
    protected int hash() {
        return ghost.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && ghost.equals(((GhostMapIngredient) o).ghost);
    }

    @Override
    public String toString() {
        return "GhostMapIngredient{" + ghost + "}";
    }
}
