package com.yiran.minecraft.gtmqol.integration.ae2;

import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;

public interface ISticky {
    public boolean isSticky();

    default boolean shouldStick(AEKey key, IActionSource source) {
        return false;
    }

    public void setSticky(boolean sticky);
}
