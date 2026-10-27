package com.yiran.minecraft.gtmqol.common.autobuild;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * What the settings window offers for a pattern. Computed the same way on both sides from the multiblock definition,
 * so the client builds the window from it and the server validates {@link BuildSettings} against it.
 *
 * @param slices  block pattern slices whose repeat count can change
 * @param bounds  the expandable pattern's (min, max) per dimension, in its own order
 * @param choices pattern chars that take at least two different non-ability blocks
 */
public record BuildOptions(List<Slice> slices, List<IntIntPair> bounds, List<ChoicePoint> choices) {

    public record Slice(int index, int min, int max) {}

    /** {@code index} is the predicate's index in {@code root.expand()}. */
    public record Option(int index, BasePredicate predicate, BlockInfo info) {

        public Block block() {
            return info.getBlockState().getBlock();
        }
    }

    public record ChoicePoint(char symbol, MultiPredicate root, List<Option> options) {

        public @Nullable Option find(BuildSettings.BlockChoice choice) {
            for (Option option : options) {
                if (option.index() == choice.predicate() && option.block() == choice.block()) return option;
            }
            return null;
        }
    }

    public static BuildOptions of(IBlockPattern pattern) {
        if (pattern instanceof BlockPattern blockPattern) {
            return new BuildOptions(slices(blockPattern), List.of(), choices(blockPattern));
        }
        if (pattern instanceof ExpandablePattern expandable && expandable.getBoundsConstraints() != null) {
            return new BuildOptions(List.of(), expandable.getBoundsConstraints().apply(), List.of());
        }
        return new BuildOptions(List.of(), List.of(), List.of());
    }

    public @Nullable ChoicePoint choice(char symbol) {
        for (ChoicePoint point : choices) {
            if (point.symbol() == symbol) return point;
        }
        return null;
    }

    public boolean isEmpty() {
        return slices.isEmpty() && bounds.stream().allMatch(b -> b.leftInt() >= b.rightInt()) && choices.isEmpty();
    }

    private static List<Slice> slices(BlockPattern pattern) {
        PatternSlice[] slices = pattern.getSlices();
        List<Slice> result = new ArrayList<>();
        for (int i = 0; i < slices.length; i++) {
            if (slices[i].getMinRepeats() < slices[i].getMaxRepeats()) {
                result.add(new Slice(i, slices[i].getMinRepeats(), slices[i].getMaxRepeats()));
            }
        }
        return result;
    }

    private static List<ChoicePoint> choices(BlockPattern pattern) {
        // sorted so the window does not depend on the map's iteration order
        char[] symbols = pattern.getPredicates().keySet().toCharArray();
        Arrays.sort(symbols);
        // a predicate object shared by two chars is one choice, listed under the first char
        Set<MultiPredicate> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<ChoicePoint> result = new ArrayList<>();
        for (char symbol : symbols) {
            MultiPredicate root = pattern.getPredicates().get(symbol);
            if (root.isAir() || root.isAny() || root.isController() || !seen.add(root)) continue;
            List<BasePredicate> expanded = root.expand();
            List<Option> options = new ArrayList<>();
            for (int i = 0; i < expanded.size(); i++) {
                BasePredicate predicate = expanded.get(i);
                if (isAbility(predicate)) continue;
                Set<Block> blocks = Collections.newSetFromMap(new IdentityHashMap<>());
                for (BlockInfo candidate : predicate.getCandidates()) {
                    Block block = candidate.getBlockState().getBlock();
                    if (candidate.isAir() || block.asItem() == Items.AIR || !blocks.add(block)) continue;
                    options.add(new Option(i, predicate, candidate));
                }
            }
            if (options.size() >= 2) result.add(new ChoicePoint(symbol, root, List.copyOf(options)));
        }
        return result;
    }

    /**
     * Hatches, buses and other parts: every candidate is a machine block. The controller counts too, but its position
     * is never placed anyway.
     */
    // Untested: abilities by machine block; auto-build leaves hatch/bus/muffler spots empty, casings elsewhere
    static boolean isAbility(BasePredicate predicate) {
        List<BlockInfo> candidates = predicate.getCandidates();
        return !candidates.isEmpty() &&
                candidates.stream().allMatch(c -> c.getBlockState().getBlock() instanceof MetaMachineBlock);
    }
}
