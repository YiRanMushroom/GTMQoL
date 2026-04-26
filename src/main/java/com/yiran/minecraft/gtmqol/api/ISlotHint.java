package com.yiran.minecraft.gtmqol.api;

public interface ISlotHint {
    default boolean qol$isCatalystSlot() {
        return false;
    }
}
