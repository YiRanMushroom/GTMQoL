package com.yiran.minecraft.gtmqol.common.autobuild;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * What to build. Keyed by things that stay stable for a multiblock type (slice index, the expandable pattern's bounds
 * order), never by world positions, so the client can later keep one per multiblock type and send it with the click.
 * The server only treats this as a wish: {@link StructurePlanner} clamps it to the pattern.
 * <p>
 * Empty means the pattern's defaults: minimum repeats, default bounds. Block choices (pattern char + predicate index →
 * block) and ability counts are added with the config window.
 */
public record BuildSettings(Int2IntMap sliceRepeats, IntList dimensions) {

    public static final BuildSettings DEFAULTS = new BuildSettings(Int2IntMaps.EMPTY_MAP, IntLists.EMPTY_LIST);
}
