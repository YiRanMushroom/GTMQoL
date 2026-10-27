package com.yiran.minecraft.gtmqol.common.autobuild;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
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

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Turns a controller's default structure into the list of blocks to place, with GTCEu's own structure helpers (the
 * same ones the terminal and the shift-click preview use).
 * <p>
 * Abilities (hatches, buses, mufflers, ...) are never placed: a position that also takes a normal block gets that
 * block, a position that only takes abilities is left empty. An ability is a predicate whose candidates are all
 * machine blocks.
 * <p>
 * A block choice from the settings becomes GTCEu's block preference for its char, and the char's other non-ability
 * predicates are dropped. If one of those had a minimum count, the structure will not form; that is the player's
 * choice.
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

        MultiblockSchemaInfo info = new MultiblockSchemaInfo(controller.getDefinition());
        // the predicates' minimum counts, not the preview's
        info.setIgnorePreviewCount(true);
        Set<Block> abilityBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
        AbstractStructureHelper helper = createHelper(pattern, settings, info, abilityBlocks);
        Map<BlockPos, BlockInfo> structure = new HashMap<>();
        helper.populate(info, structure, pattern, null, front, up, flipped);

        BlockPos controllerLocal = findController(controller, helper, pattern, structure, front, up, flipped);
        BlockPos origin = controller.getBlockPos().subtract(controllerLocal);
        List<Entry> entries = new ArrayList<>(structure.size());
        for (var entry : structure.entrySet()) {
            BlockState state = entry.getValue().getBlockState();
            if (state.isAir() || entry.getKey().equals(controllerLocal)) continue;
            if (abilityBlocks.contains(state.getBlock())) continue;
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
                                                        MultiblockSchemaInfo info, Set<Block> abilityBlocks) {
        // Both helpers are made for subclassing (protected constructors) and take each char's predicate from
        // getPredicatesFromPattern, so they get filtered copies there.
        if (pattern instanceof BlockPattern blockPattern) {
            PatternSlice[] slices = blockPattern.getSlices();
            Int2IntMap repeats = new Int2IntArrayMap();
            for (int i = 0; i < slices.length; i++) {
                int min = slices[i].getMinRepeats();
                int max = Math.max(min, slices[i].getMaxRepeats());
                repeats.put(i, Mth.clamp(settings.sliceRepeats().getOrDefault(i, min), min, max));
            }
            Char2ObjectMap<MultiPredicate> predicates = filter(blockPattern.getPredicates(),
                    applyChoices(blockPattern, settings, info), abilityBlocks);
            return new BlockPatternHelper(repeats) {

                @Override
                protected Char2ObjectMap<MultiPredicate> getPredicatesFromPattern(IBlockPattern pattern) {
                    return predicates;
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
            Char2ObjectMap<MultiPredicate> predicates = filter(expandable.getSymbolMap(), Char2ObjectMaps.emptyMap(),
                    abilityBlocks);
            return new ExpandablePatternHelper(dimensions) {

                @Override
                protected Char2ObjectMap<MultiPredicate> getPredicatesFromPattern(IBlockPattern pattern) {
                    return predicates;
                }
            };
        }
        throw new IllegalStateException("unsupported pattern type " + pattern.getClass().getSimpleName());
    }

    /**
     * The chosen predicate per char, also set as GTCEu's block preference. Choices that are not offered by
     * {@link BuildOptions} (stale client, other pattern) are ignored.
     */
    private static Char2ObjectMap<BasePredicate> applyChoices(BlockPattern pattern, BuildSettings settings,
                                                              MultiblockSchemaInfo info) {
        Char2ObjectMap<BasePredicate> chosen = new Char2ObjectOpenHashMap<>();
        if (settings.blocks().isEmpty()) return chosen;
        BuildOptions options = BuildOptions.of(pattern);
        for (var entry : settings.blocks().char2ObjectEntrySet()) {
            BuildOptions.ChoicePoint point = options.choice(entry.getCharKey());
            BuildOptions.Option option = point == null ? null : point.find(entry.getValue());
            if (option == null) continue;
            chosen.put(entry.getCharKey(), option.predicate());
            info.getBlockPreferences().put(entry.getCharKey(), option.info());
        }
        return chosen;
    }

    /**
     * Per char, drops the ability predicates and, with a block chosen, the other non-ability predicates. A char that
     * only takes abilities keeps them, so GTCEu can still fill it (it fails otherwise); what it puts there is dropped
     * afterwards by block, collected into {@code abilityBlocks}.
     */
    // Untested: filtering for GTCEu #5337; auto-build a fixed and an expandable multiblock, and a block choice
    private static Char2ObjectMap<MultiPredicate> filter(Char2ObjectMap<MultiPredicate> predicates,
                                                         Char2ObjectMap<BasePredicate> chosen,
                                                         Set<Block> abilityBlocks) {
        Char2ObjectMap<MultiPredicate> filtered = new Char2ObjectOpenHashMap<>(predicates.size());
        for (var entry : predicates.char2ObjectEntrySet()) {
            MultiPredicate root = entry.getValue();
            boolean abilityOnly = true;
            for (BasePredicate predicate : root.expand()) {
                if (!BuildOptions.isAbility(predicate)) {
                    abilityOnly = false;
                    continue;
                }
                for (BlockInfo candidate : predicate.getCandidates()) {
                    abilityBlocks.add(candidate.getBlockState().getBlock());
                }
            }
            BasePredicate choice = chosen.get(entry.getCharKey());
            MultiPredicate kept = abilityOnly ? null :
                    keep(root, p -> !BuildOptions.isAbility(p) && (choice == null || p == choice));
            filtered.put(entry.getCharKey(), kept == null ? root : kept);
        }
        return filtered;
    }

    /** The node with only the predicates {@code keep} accepts, or null if none are left. */
    private static @Nullable MultiPredicate keep(MultiPredicate node, Predicate<BasePredicate> keep) {
        List<BasePredicate> predicates = node.predicates().stream().filter(keep).toList();
        List<MultiPredicate> children = new ArrayList<>();
        for (MultiPredicate child : node.children()) {
            MultiPredicate kept = keep(child, keep);
            if (kept != null) children.add(kept);
        }
        if (predicates.isEmpty() && children.isEmpty()) return null;
        // keeps the node's counts, controller and air flags
        return node.withContents(children, predicates);
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
}
