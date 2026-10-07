package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.prioritylist.IPartitionList;
import com.yiran.minecraft.gtmqol.integration.ae2.ISticky;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Adds the sticky flag to every storage bus handler (AE2's and ExtendedAE's both extend this). */
@Mixin(value = MEInventoryHandler.class, remap = false)
public abstract class MEInventoryHandlerMixin implements ISticky {

    @Shadow
    private IPartitionList partitionList;

    @Shadow
    protected abstract boolean passesBlackOrWhitelist(AEKey input);

    @Unique
    private boolean gtmqol$sticky;

    @Override
    public boolean isSticky() {
        return gtmqol$sticky;
    }

    @Override
    public void setSticky(boolean sticky) {
        gtmqol$sticky = sticky;
    }

    @Override
    public boolean shouldStick(AEKey key, IActionSource source) {
        return gtmqol$sticky && !partitionList.isEmpty() && passesBlackOrWhitelist(key);
    }
}
