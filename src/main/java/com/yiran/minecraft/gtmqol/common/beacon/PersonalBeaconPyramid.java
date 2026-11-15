package com.yiran.minecraft.gtmqol.common.beacon;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandableMultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;

import java.util.List;

/**
 * The Personal Beacon's structure, shaped like a vanilla beacon's: the controller alone on top of a solid pyramid.
 * Layer {@code d} below the controller is a square reaching {@code d} blocks from the middle: 3 × 3 right below
 * the controller, then 5 × 5, and so on. A layer's outer ring is what shows of it, and may hold parts.
 */
public final class PersonalBeaconPyramid {

    public static final int MIN_LAYERS = 1;
    public static final int MAX_LAYERS = 7;

    private PersonalBeaconPyramid() {}

    /**
     * The pyramid, still without its blocks: {@code 'c'} is the controller, {@code 's'} a layer's outer ring,
     * {@code 'i'} a block inside and {@code ' '} the rest of the box around the pyramid.
     */
    public static ExpandableMultiblockPatternBuilder pattern() {
        // up, down, left, right, front, back of the controller. Only the number of layers varies: the box is always as
        // wide as the largest pyramid, so the preview and auto-build offer that one size and nothing else.
        IntIntPair width = IntIntPair.of(MAX_LAYERS, MAX_LAYERS);
        return ExpandableMultiblockPatternBuilder
                .start(RelativeDirection.UP, RelativeDirection.RIGHT, RelativeDirection.FRONT)
                .boundsProvider(bounds())
                .constraintProvider(() -> List.of(IntIntPair.of(0, 0), IntIntPair.of(MIN_LAYERS, MAX_LAYERS),
                        width, width, width, width))
                .predicateProvider((pos, bounds) -> key(pos));
    }

    /** {@code pos}: up, right, front of the controller. */
    private static char key(BlockPos pos) {
        if (pos.equals(BlockPos.ZERO)) return 'c';
        int layer = -pos.getX();
        int ring = Math.max(Math.abs(pos.getY()), Math.abs(pos.getZ()));
        if (layer <= 0 || ring > layer) return ' ';
        return ring == layer ? 's' : 'i';
    }

    private static ExpandablePattern.BoundsProvider bounds() {
        return (level, controllerPos, front, up) -> {
            int layers = layers(level, controllerPos.immutable());
            return new IntArrayList(new int[] { 0, layers, MAX_LAYERS, MAX_LAYERS, MAX_LAYERS, MAX_LAYERS });
        };
    }

    /**
     * How many layers there are below the controller: every layer that is complete, so what the pyramid stands on
     * is not taken for one more. The fewest when there are fewer, for the preview and auto-build.
     */
    public static int layers(Level level, BlockPos controller) {
        int layers = 0;
        while (layers < MAX_LAYERS && isLayer(level, controller, layers + 1)) layers++;
        return Math.max(layers, MIN_LAYERS);
    }

    private static boolean isLayer(Level level, BlockPos controller, int layer) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -layer; x <= layer; x++) {
            for (int z = -layer; z <= layer; z++) {
                pos.setWithOffset(controller, x, -layer, z);
                Block block = level.getBlockState(pos).getBlock();
                if (!GTBlocks.CASING_TITANIUM_STABLE.is(block) && !(block instanceof MetaMachineBlock)) return false;
            }
        }
        return true;
    }
}
