package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import appeng.api.parts.IPartItem;
import appeng.parts.automation.UpgradeablePart;
import com.glodblock.github.extendedae.common.parts.base.PartSpecialStorageBus;
import com.yiran.minecraft.gtmqol.integration.ae2.ISticky;
import com.yiran.minecraft.gtmqol.integration.ae2.StickyCardItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Same as {@link StorageBusPartMixin}, for ExtendedAE's mod / precise / tag storage buses. */
@Mixin(value = PartSpecialStorageBus.class, remap = false)
public abstract class PartSpecialStorageBusMixin extends UpgradeablePart {

    @Shadow
    @Final
    protected PartSpecialStorageBus.StorageBusInventory handler;

    public PartSpecialStorageBusMixin(IPartItem<?> partItem) {
        super(partItem);
    }

    @Inject(method = "updateTarget",
            at = @At(value = "INVOKE",
                     target = "Lcom/glodblock/github/extendedae/common/parts/base/PartSpecialStorageBus$StorageBusInventory;setVoidOverflow(Z)V",
                     shift = At.Shift.AFTER))
    private void gtmqol$applySticky(boolean forceFullUpdate, CallbackInfo ci) {
        if (StickyCardItem.STICKY_CARD != null) {
            ((ISticky) handler).setSticky(isUpgradedWith(StickyCardItem.STICKY_CARD));
        }
    }
}
