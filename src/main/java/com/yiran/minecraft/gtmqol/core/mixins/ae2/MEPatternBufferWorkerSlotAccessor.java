package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The slot of a pattern buffer worker (package-private in gtceu), for the {@code MEPatternBuffer*StackLike} mixins.
 * Its own accessor (not the recipedb one, whose config may be off), with another name so the two don't collide.
 */
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$Worker", remap = false)
public interface MEPatternBufferWorkerSlotAccessor {

    @Accessor("slot")
    MEPatternBufferPartMachine.InternalSlot gtmqol$getWorkerSlot();
}
