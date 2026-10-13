package com.yiran.minecraft.gtmqol.common.autobuild;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockPatternHelper;
import com.gregtechceu.gtceu.api.multiblock.util.ExpandablePatternHelper;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns a controller's default structure into the list of blocks to place, with GTCEu's own structure helpers (the
 * same ones the terminal and the shift-click preview use).
 * <p>
 * Abilities (hatches, buses, mufflers, ...) are never placed: a position that also takes a normal block gets that
 * block, a position that only takes abilities is left empty. "Ability" is decided by the predicate's type name, which
 * is what {@code Predicates.abilities}/{@code ability} produce. That is a heuristic, but this is only the default.
 */
public final class StructurePlanner {

    /** One block to place, in world coordinates. */
    public record Entry(BlockPos pos, BlockState state) {}

    private StructurePlanner() {}

    /**
     * Runs on the server thread. Sorted bottom-up, then nearest to the controller first.
     *
     * @throws IllegalStateException if GTCEu cannot lay out the pattern; the message is shown to the player
     */
    public static List<Entry> plan(MultiblockControllerMachine controller, BuildSettings settings) {
        long start = System.nanoTime();
        IBlockPattern pattern = controller.getStructurePatterns().get(MultiblockControllerMachine.DEFAULT_STRUCTURE);
        if (pattern == null) throw new IllegalStateException("no default structure");
        Direction front = controller.getFrontFacing();
        Direction up = controller.getUpwardsFacing();
        boolean flipped = controller.isFlipped();

        AbilityFilter abilities = new AbilityFilter();
        AbstractStructureHelper helper = createHelper(pattern, settings, abilities);
        Map<BlockPos, BlockInfo> structure = new HashMap<>();
        helper.populate(structure, pattern, null, front, up, flipped);

        BlockPos controllerLocal = findController(controller, helper, pattern, structure, front, up, flipped);
        BlockPos origin = controller.getBlockPos().subtract(controllerLocal);
        List<Entry> entries = new ArrayList<>(structure.size());
        for (var entry : structure.entrySet()) {
            BlockState state = entry.getValue().getBlockState();
            if (state.isAir() || entry.getKey().equals(controllerLocal)) continue;
            if (abilities.isAbilityBlock(state.getBlock())) continue;
            entries.add(new Entry(origin.offset(entry.getKey()), state));
        }
        BlockPos center = controller.getBlockPos();
        entries.sort(Comparator.<Entry>comparingInt(e -> e.pos().getY())
                .thenComparingDouble(e -> e.pos().distSqr(center)));

        GTMQoL.LOGGER.info("Auto-build: planned {} blocks ({} in pattern) for {} at {} in {} ms", entries.size(),
                structure.size(), controller.getDefinition().getId(), center, (System.nanoTime() - start) / 1_000_000);
        return entries;
    }

    private static AbstractStructureHelper createHelper(IBlockPattern pattern, BuildSettings settings,
                                                        AbilityFilter abilities) {
        // Both helpers are made for subclassing (protected constructors, overridable min/max counts). Ability counts
        // go through the overrides because an expandable pattern only hands out its predicates while populating.
        if (pattern instanceof BlockPattern blockPattern) {
            PatternSlice[] slices = blockPattern.getSlices();
            Int2IntMap repeats = new Int2IntArrayMap();
            for (int i = 0; i < slices.length; i++) {
                int min = slices[i].getMinRepeats();
                int max = Math.max(min, slices[i].getMaxRepeats());
                repeats.put(i, Mth.clamp(settings.sliceRepeats().getOrDefault(i, min), min, max));
            }
            return new BlockPatternHelper(repeats) {

                @Override
                protected int getMinCount(MultiPredicate predicate, BasePredicate basePredicate) {
                    return abilities.suppress(predicate, basePredicate) ? 0 :
                            super.getMinCount(predicate, basePredicate);
                }

                @Override
                protected int getMaxCount(MultiPredicate predicate, BasePredicate basePredicate) {
                    return abilities.suppress(predicate, basePredicate) ? 0 :
                            super.getMaxCount(predicate, basePredicate);
                }
            };
        }
        if (pattern instanceof ExpandablePattern expandable) {
            List<IntIntPair> bounds = expandable.getBoundsConstraints() == null ? List.of() :
                    expandable.getBoundsConstraints().apply();
            IntList dimensions = new IntArrayList(bounds.size());
            boolean useSettings = settings.dimensions().size() == bounds.size();
            for (int i = 0; i < bounds.size(); i++) {
                IntIntPair minMax = bounds.get(i);
                int wanted = useSettings ? settings.dimensions().getInt(i) : minMax.leftInt();
                dimensions.add(Mth.clamp(wanted, minMax.leftInt(), Math.max(minMax.leftInt(), minMax.rightInt())));
            }
            return new ExpandablePatternHelper(dimensions) {

                @Override
                protected int getMinCount(MultiPredicate predicate, BasePredicate basePredicate) {
                    return abilities.suppress(predicate, basePredicate) ? 0 :
                            super.getMinCount(predicate, basePredicate);
                }

                @Override
                protected int getMaxCount(MultiPredicate predicate, BasePredicate basePredicate) {
                    return abilities.suppress(predicate, basePredicate) ? 0 :
                            super.getMaxCount(predicate, basePredicate);
                }
            };
        }
        throw new IllegalStateException("unsupported pattern type " + pattern.getClass().getSimpleName());
    }

    private static BlockPos findController(MultiblockControllerMachine controller, AbstractStructureHelper helper,
                                           IBlockPattern pattern, Map<BlockPos, BlockInfo> structure,
                                           Direction front, Direction up, boolean flipped) {
        Block block = controller.getBlockState().getBlock();
        List<BlockPos> candidates = structure.entrySet().stream()
                .filter(e -> e.getValue().getBlockState().is(block))
                .map(Map.Entry::getKey)
                .toList();
        if (candidates.size() == 1) return candidates.get(0);
        // getPredicateFromPos re-flattens the whole pattern each call, so only for the (rare) ambiguous case
        for (BlockPos pos : candidates) {
            if (helper.getPredicateFromPos(pattern, pos, front, up, flipped).isController()) return pos;
        }
        throw new IllegalStateException("controller not found in the structure");
    }

    /**
     * Sets ability counts to 0 unless the whole predicate is abilities. Such positions keep their counts so GTCEu can
     * still fill them (it throws otherwise); what it puts there is dropped afterwards by block.
     */
    private static final class AbilityFilter {

        private final Map<MultiPredicate, Boolean> abilityOnly = new IdentityHashMap<>();
        private final Set<BasePredicate> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<Block> abilityBlocks = Collections.newSetFromMap(new IdentityHashMap<>());

        boolean suppress(MultiPredicate predicate, BasePredicate basePredicate) {
            if (!isAbility(basePredicate)) return false;
            if (seen.add(basePredicate)) {
                for (BlockInfo candidate : basePredicate.getCandidates()) {
                    abilityBlocks.add(candidate.getBlockState().getBlock());
                }
            }
            MultiPredicate root = root(predicate);
            return !abilityOnly.computeIfAbsent(root,
                    r -> r.expand().stream().allMatch(StructurePlanner::isAbility));
        }

        boolean isAbilityBlock(Block block) {
            return abilityBlocks.contains(block);
        }

        private static MultiPredicate root(MultiPredicate predicate) {
            while (predicate.getParent() != null) predicate = predicate.getParent();
            return predicate;
        }
    }

    private static boolean isAbility(BasePredicate predicate) {
        String type = predicate.getTypeName();
        return "Ability".equals(type) || "TieredAbility".equals(type);
    }
}
