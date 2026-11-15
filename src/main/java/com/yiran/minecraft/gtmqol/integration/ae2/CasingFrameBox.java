package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandableMultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.MAX;
import static com.gregtechceu.gtceu.api.GTValues.ULV;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS;

/**
 * The structure the Assembling Matrix and the ME Data Center share: a hollow box sized like a cleanroom, the controller
 * in the middle of the front's bottom edge. Its edges are machine casings of one tier, which the controller and the
 * parts on the edges show; its faces and inside are the machine's own.
 */
public final class CasingFrameBox {

    /** Which casing tier the controller shows; before forming, the one of its item. */
    public static final IntegerProperty CASING_TIER = IntegerProperty.create("casing_tier", ULV, MAX);
    /** {@link #tier}: casings of more than one tier. */
    public static final int MIXED = -2;

    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 7;
    public static final int MIN_DEPTH = 2;
    public static final int MAX_DEPTH = 14;

    /** A controller of such a box: {@link com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixPartRender}. */
    public interface Controller {

        /** The casing of the frame, which is what its parts show. */
        BlockState casingState();
    }

    private CasingFrameBox() {}

    /** The casing frame in every tier by index, ULV first. */
    public static List<BlockEntry<Block>> casings() {
        return List.of(GTBlocks.MACHINE_CASING_ULV, GTBlocks.MACHINE_CASING_LV, GTBlocks.MACHINE_CASING_MV,
                GTBlocks.MACHINE_CASING_HV, GTBlocks.MACHINE_CASING_EV, GTBlocks.MACHINE_CASING_IV,
                GTBlocks.MACHINE_CASING_LuV, GTBlocks.MACHINE_CASING_ZPM, GTBlocks.MACHINE_CASING_UV,
                GTBlocks.MACHINE_CASING_UHV, GTBlocks.MACHINE_CASING_UEV, GTBlocks.MACHINE_CASING_UIV,
                GTBlocks.MACHINE_CASING_UXV, GTBlocks.MACHINE_CASING_OpV, GTBlocks.MACHINE_CASING_MAX);
    }

    public static Block[] casingBlocks() {
        return casings().stream().map(BlockEntry::get).toArray(Block[]::new);
    }

    /**
     * The box, still without its blocks: {@code 'c'} is the controller, {@code 'e'} an edge, {@code 'g'} a face and
     * {@code 'i'} the inside.
     */
    public static ExpandableMultiblockPatternBuilder box() {
        // as the cleanroom's: up, down, left, right, front, back of the controller
        return ExpandableMultiblockPatternBuilder
                .start(RelativeDirection.UP, RelativeDirection.RIGHT, RelativeDirection.FRONT)
                .boundsProvider(bounds())
                .constraintProvider(() -> List.of(IntIntPair.of(MIN_DEPTH, MAX_DEPTH), IntIntPair.of(0, 0),
                        IntIntPair.of(MIN_RADIUS, MAX_RADIUS), IntIntPair.of(MIN_RADIUS, MAX_RADIUS),
                        IntIntPair.of(0, 0), IntIntPair.of(MIN_DEPTH, MAX_DEPTH)))
                .predicateProvider((pos, bounds) -> {
                    if (pos.equals(BlockPos.ZERO)) return 'c';
                    int faces = 0;
                    if (pos.getX() == bounds.get(0) || pos.getX() == -bounds.get(1)) faces++;
                    if (pos.getY() == -bounds.get(2) || pos.getY() == bounds.get(3)) faces++;
                    if (pos.getZ() == bounds.get(4) || pos.getZ() == -bounds.get(5)) faces++;
                    return faces >= 2 ? 'e' : faces == 1 ? 'g' : 'i';
                });
    }

    /**
     * Follows the frame from the controller: along its edge to the left and right, then from the left corner back and
     * up. A frame can touch other casings, so its corners are told by the edge that turns off there.
     */
    public static ExpandablePattern.BoundsProvider bounds() {
        return (level, controllerPos, front, up) -> {
            Direction left = front.getCounterClockWise();
            Direction back = front.getOpposite();
            BlockPos controller = controllerPos.immutable();
            int l = length(level, controller, left, back, MAX_RADIUS);
            int r = length(level, controller, left.getOpposite(), back, MAX_RADIUS);
            BlockPos corner = controller.relative(left, l);
            int b = length(level, corner, back, left.getOpposite(), MAX_DEPTH);
            int u = length(level, corner, up, left.getOpposite(), MAX_DEPTH);
            if (l < MIN_RADIUS || r < MIN_RADIUS || b < MIN_DEPTH || u < MIN_DEPTH) {
                // the smallest that can work: two blocks inside, for the preview and auto-build
                return new IntArrayList(new int[] { MIN_DEPTH, 0, MIN_RADIUS, MIN_RADIUS, 0, MIN_DEPTH + 1 });
            }
            return new IntArrayList(new int[] { u, 0, l, r, 0, b });
        };
    }

    /** The controller stands in the middle of its edge, or one off it when the edge is even. */
    public static boolean isCentered(IntList bounds) {
        return Math.abs(bounds.getInt(2) - bounds.getInt(3)) <= 1;
    }

    /** The tier of the casings in a formed structure: ULV when it has none, {@link #MIXED} when they differ. */
    public static int tier(PatternState state) {
        var casings = casings();
        int tier = -1;
        for (var info : state.getCache().values()) {
            Block block = info.getBlockState().getBlock();
            for (int i = 0; i < casings.size(); i++) {
                if (!casings.get(i).is(block)) continue;
                if (tier >= 0 && tier != i) return MIXED;
                tier = i;
            }
        }
        return Math.max(tier, ULV);
    }

    /** A machine's workable overlays on the casing of each tier. */
    public static MachineBuilder.ModelInitializer controllerModel(ResourceLocation overlayDirectory) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDirectory, prov.getExistingFileHelper());
            builder.forAllStatesModelsExcept(state -> {
                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(GTMachineModels.SIDED_SIDED_OVERLAY_MODEL));
                GTMachineModels.tieredHullTextures(model, state.getValue(CASING_TIER));
                return GTMachineModels.addWorkableOverlays(overlays, state.getValue(RECIPE_LOGIC_STATUS), model);
            }, IS_FORMED);
        };
    }

    /** How far the edge runs along {@code along}; at its end one edge turns towards {@code turn}. */
    private static int length(Level level, BlockPos start, Direction along, Direction turn, int max) {
        for (int i = 1; i <= max; i++) {
            BlockPos pos = start.relative(along, i);
            if (!isFrame(level.getBlockState(pos))) return i - 1;
            if (isFrame(level.getBlockState(pos.relative(turn)))) return i;
        }
        return max;
    }

    private static boolean isFrame(BlockState state) {
        if (state.getBlock() instanceof MetaMachineBlock) return true;
        return casings().stream().anyMatch(casing -> casing.is(state.getBlock()));
    }
}
