package com.yiran.minecraft.gtmqol.integration.ae2.batch;

import appeng.api.crafting.IPatternDetails;

/**
 * A crafting provider that takes many crafts of a pattern in one push, as a {@link PatternBatch}. Our pattern buffers
 * and the assembling matrix; AE2's pattern providers don't know that pattern and are not one of these.
 */
public interface BatchCraftingProvider {

    /** The most crafts of this pattern it takes in one push; below 2, it doesn't want batches of it. */
    long batchLimit(IPatternDetails pattern);
}
