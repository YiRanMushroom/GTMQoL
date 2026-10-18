package com.yiran.minecraft.gtmqol.common.blockprovider;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * Provides the block at {@code position} to recipes as a {@link BlockProviderRecipeCapability} input; nothing is
 * consumed. A controller trait, so multiblocks pick it up when they form. The machine calls
 * {@link #notifyListeners()} when that block may have changed, so an idle machine looks for a recipe again.
 */
public class BlockProviderTrait extends NotifiableRecipeHandlerTrait<Block> {

    private final Function<MetaMachine, BlockPos> position;

    public BlockProviderTrait(Function<MetaMachine, BlockPos> position) {
        this.position = position;
    }

    public BlockPos getPosition() {
        return position.apply(getMachine());
    }

    private @Nullable Block getBlock() {
        Level level = getMachine().getLevel();
        if (level == null) return null;
        BlockState state = level.getBlockState(getPosition());
        return state.isAir() ? null : state.getBlock();
    }

    @Override
    public @NotNull List<Block> handleRecipeInner(IO io, @Nullable GTRecipe recipe, List<Block> left,
                                                  boolean simulate) {
        if (io != IO.IN) return left;
        Block block = getBlock();
        if (block != null) left.removeIf(b -> b == block);
        return left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        Block block = getBlock();
        return block == null ? List.of() : List.of(block);
    }

    @Override
    public double getTotalContentAmount() {
        return getBlock() == null ? 0 : 1;
    }

    @Override
    public RecipeCapability<Block> getCapability() {
        return BlockProviderRecipeCapability.CAP;
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }
}
