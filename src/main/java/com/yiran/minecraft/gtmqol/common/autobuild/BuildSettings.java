package com.yiran.minecraft.gtmqol.common.autobuild;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * What to build. Keyed by things that stay stable for a multiblock type (slice index, the expandable pattern's bounds
 * order, pattern char + index in {@code MultiPredicate.expand()}), never by world positions, so the client keeps one
 * per multiblock type and sends it with the click. The server only treats this as a wish: {@link StructurePlanner}
 * clamps it to the pattern and drops choices that are not in {@link BuildOptions}.
 * <p>
 * Empty means the pattern's defaults: minimum repeats, minimum bounds, GTCEu's first candidates.
 */
public record BuildSettings(Int2IntMap sliceRepeats, IntList dimensions, Char2ObjectMap<BlockChoice> blocks) {

    public static final BuildSettings DEFAULTS = new BuildSettings(Int2IntMaps.EMPTY_MAP, IntLists.EMPTY_LIST,
            Char2ObjectMaps.emptyMap());

    private static final StreamCodec<RegistryFriendlyByteBuf, Block> BLOCK = ByteBufCodecs.registry(Registries.BLOCK);

    /** The block {@code block} of the predicate at {@code predicate} in the char's {@code expand()} list. */
    public record BlockChoice(int predicate, Block block) {}

    /** Empty and mutable, for the client side store. */
    public static BuildSettings create() {
        return new BuildSettings(new Int2IntArrayMap(), new IntArrayList(), new Char2ObjectArrayMap<>());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sliceRepeats.size());
        for (var entry : sliceRepeats.int2IntEntrySet()) {
            buf.writeVarInt(entry.getIntKey());
            buf.writeVarInt(entry.getIntValue());
        }
        buf.writeVarInt(dimensions.size());
        for (int i = 0; i < dimensions.size(); i++) buf.writeVarInt(dimensions.getInt(i));
        buf.writeVarInt(blocks.size());
        for (var entry : blocks.char2ObjectEntrySet()) {
            buf.writeChar(entry.getCharKey());
            buf.writeVarInt(entry.getValue().predicate());
            BLOCK.encode(buf, entry.getValue().block());
        }
    }

    public static BuildSettings read(RegistryFriendlyByteBuf buf) {
        BuildSettings settings = create();
        int repeats = buf.readVarInt();
        for (int i = 0; i < repeats; i++) settings.sliceRepeats.put(buf.readVarInt(), buf.readVarInt());
        int dimensions = buf.readVarInt();
        for (int i = 0; i < dimensions; i++) settings.dimensions.add(buf.readVarInt());
        int blocks = buf.readVarInt();
        for (int i = 0; i < blocks; i++) {
            char symbol = buf.readChar();
            int predicate = buf.readVarInt();
            settings.blocks.put(symbol, new BlockChoice(predicate, BLOCK.decode(buf)));
        }
        return settings;
    }
}
