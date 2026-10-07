package com.yiran.minecraft.gtmqol.integration.ae2;

import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;

/** Added to AE2's {@code MEInventoryHandler} by a mixin; set by storage buses with a sticky card. */
public interface ISticky {

    boolean isSticky();

    void setSticky(boolean sticky);

    boolean shouldStick(AEKey key, IActionSource source);
}
