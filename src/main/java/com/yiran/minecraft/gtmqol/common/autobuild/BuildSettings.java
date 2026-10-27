package com.yiran.minecraft.gtmqol.common.autobuild;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * What to build, as edited in GTCEu's multiblock preview ({@link AutoBuildPanel}): slice repeats, the expandable
 * pattern's dimensions, a block per pattern char, and a block per position in the controller's layout (only for the
 * controller they were picked on). The server only treats this as a wish: {@link StructurePlanner} clamps it to the
 * pattern and drops what doesn't fit.
 * <p>
 * Empty means the pattern's defaults: minimum repeats, minimum bounds, GTCEu's first candidates.
 */
public record BuildSettings(Int2IntMap sliceRepeats, IntList dimensions, Char2ObjectMap<BlockState> blocks,
                            Object2ObjectMap<BlockPos, BlockState> positions) {

    public static BuildSettings create() {
        return new BuildSettings(new Int2IntArrayMap(), new IntArrayList(), new Char2ObjectArrayMap<>(),
                new Object2ObjectOpenHashMap<>());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeMap(sliceRepeats, FriendlyByteBuf::writeVarInt, FriendlyByteBuf::writeVarInt);
        buf.writeCollection(dimensions, FriendlyByteBuf::writeVarInt);
        buf.writeMap(blocks, (b, symbol) -> b.writeChar(symbol), (b, state) -> b.writeVarInt(Block.getId(state)));
        buf.writeMap(positions, FriendlyByteBuf::writeBlockPos, (b, state) -> b.writeVarInt(Block.getId(state)));
    }

    public static BuildSettings read(FriendlyByteBuf buf) {
        return new BuildSettings(
                buf.readMap(Int2IntArrayMap::new, FriendlyByteBuf::readVarInt, FriendlyByteBuf::readVarInt),
                buf.readCollection(IntArrayList::new, FriendlyByteBuf::readVarInt),
                buf.readMap(Char2ObjectArrayMap::new, FriendlyByteBuf::readChar, b -> Block.stateById(b.readVarInt())),
                buf.readMap(Object2ObjectOpenHashMap::new, FriendlyByteBuf::readBlockPos,
                        b -> Block.stateById(b.readVarInt())));
    }
}
