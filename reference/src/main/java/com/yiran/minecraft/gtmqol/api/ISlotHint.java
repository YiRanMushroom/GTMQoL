package com.yiran.minecraft.gtmqol.api;

import org.jetbrains.annotations.Nullable;

public interface ISlotHint {
    default boolean qol$isCatalystSlot() {
        return false;
    }

    default @Nullable Object qol$getMatchingGroup() {
        return null; // if null, it is a unique group
    }

    default void qol$setMatchingGroup(@Nullable Object group) {
        throw new UnsupportedOperationException("This ISlotHint does not support setting matching group");
    }
}
