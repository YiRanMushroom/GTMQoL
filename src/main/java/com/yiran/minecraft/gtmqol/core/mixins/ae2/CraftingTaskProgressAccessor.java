package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The count of pushes left in a task of AE2's CPU; the class is package private, so cast its objects to this. */
@Mixin(targets = "appeng.crafting.execution.ExecutingCraftingJob$TaskProgress", remap = false)
public interface CraftingTaskProgressAccessor {

    @Accessor("value")
    long gtmqol$getValue();

    @Accessor("value")
    void gtmqol$setValue(long value);
}
