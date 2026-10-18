package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import appeng.api.crafting.IPatternDetails;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The pattern a pattern buffer worker is working on (package-private in gtceu), for {@code PatternOutputMarks}. */
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$Worker", remap = false)
public interface MEPatternBufferWorkerPatternAccessor {

    @Accessor("pattern")
    @Nullable
    IPatternDetails gtmqol$getWorkerPattern();
}
