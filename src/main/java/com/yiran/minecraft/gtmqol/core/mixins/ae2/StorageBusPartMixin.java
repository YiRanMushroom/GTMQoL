package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import appeng.api.parts.IPartItem;
import appeng.parts.automation.UpgradeablePart;
import appeng.parts.storagebus.StorageBusPart;
import com.yiran.minecraft.gtmqol.integration.ae2.ISticky;
import com.yiran.minecraft.gtmqol.integration.ae2.StickyCardItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Copies "has a sticky card" onto the handler right before AE2 applies the void card. The handler's class is
 * private in AE2; META-INF/accesstransformer.cfg opens it (ModAccessor for javac, NeoForge at runtime).
 */
@Mixin(StorageBusPart.class)
public abstract class StorageBusPartMixin extends UpgradeablePart {

    @Shadow
    @Final
    private StorageBusPart.StorageBusInventory handler;

    public StorageBusPartMixin(IPartItem<?> partItem) {
        super(partItem);
    }

    @Inject(method = "updateTarget",
            at = @At(value = "INVOKE",
                     target = "Lappeng/parts/storagebus/StorageBusPart$StorageBusInventory;setVoidOverflow(Z)V"))
    private void gtmqol$applySticky(boolean forceFullUpdate, CallbackInfo ci) {
        if (StickyCardItem.STICKY_CARD != null) {
            ((ISticky) handler).setSticky(isUpgradedWith(StickyCardItem.STICKY_CARD));
        }
    }
}
