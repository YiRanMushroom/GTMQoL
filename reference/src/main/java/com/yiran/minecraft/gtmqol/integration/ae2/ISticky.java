package com.yiran.minecraft.gtmqol.integration.ae2;

import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;

public interface ISticky {
    public boolean isSticky();

    boolean shouldStick(AEKey key, IActionSource source);

    public void setSticky(boolean sticky);
}
