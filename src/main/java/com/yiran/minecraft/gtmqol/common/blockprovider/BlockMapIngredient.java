package com.yiran.minecraft.gtmqol.common.blockprovider;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.world.level.block.Block;

/** Recipe lookup node of a {@link BlockProviderRecipeCapability} block. */
public final class BlockMapIngredient extends AbstractMapIngredient {

    private final Block block;

    public BlockMapIngredient(Block block) {
        this.block = block;
    }

    @Override
    protected int hash() {
        return block.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && block == ((BlockMapIngredient) o).block;
    }

    @Override
    public String toString() {
        return "BlockMapIngredient{" + block + "}";
    }
}
