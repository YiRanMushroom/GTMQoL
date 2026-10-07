package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import appeng.api.parts.IPartItem;
import appeng.parts.automation.UpgradeablePart;
import appeng.parts.storagebus.StorageBusPart;
import com.yiran.minecraft.gtmqol.integration.ae2.ISticky;
import com.yiran.minecraft.gtmqol.integration.ae2.StickyCardItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * Copies "has a sticky card" onto the handler right before AE2 applies the void card. The handler's class is
 * private, so (as in v7) the field is read by reflection; neither a typed {@code @Shadow} nor a
 * {@code @WrapOperation} receiver of the supertype matches the private type.
 */
@Mixin(StorageBusPart.class)
public abstract class StorageBusPartMixin extends UpgradeablePart {

    @Unique
    private static final Field gtmqol$HANDLER;

    static {
        try {
            gtmqol$HANDLER = StorageBusPart.class.getDeclaredField("handler");
            gtmqol$HANDLER.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public StorageBusPartMixin(IPartItem<?> partItem) {
        super(partItem);
    }

    @Inject(method = "updateTarget",
            at = @At(value = "INVOKE",
                     target = "Lappeng/parts/storagebus/StorageBusPart$StorageBusInventory;setVoidOverflow(Z)V"))
    private void gtmqol$applySticky(boolean forceFullUpdate, CallbackInfo ci) {
        if (StickyCardItem.STICKY_CARD == null) return;
        try {
            ((ISticky) gtmqol$HANDLER.get(this)).setSticky(isUpgradedWith(StickyCardItem.STICKY_CARD));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
