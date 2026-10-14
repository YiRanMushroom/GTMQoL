package com.yiran.minecraft.gtmqol.mixin.applied_energistics;

import appeng.api.parts.IPartItem;
import appeng.api.storage.MEStorage;
import appeng.parts.automation.UpgradeablePart;
import appeng.parts.storagebus.StorageBusPart;
import com.yiran.minecraft.gtmqol.common.item.StickyCardItem;
import com.yiran.minecraft.gtmqol.data.QoLItems;
import com.yiran.minecraft.gtmqol.integration.ae2.ISticky;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Restriction(
        require = @Condition(type = Condition.Type.MOD, value = "ae2")
)
@Mixin(StorageBusPart.class)
public abstract class StorageBusPart$AddSticky$Mixin extends UpgradeablePart {
    @Unique
    private static Field qol$handlerField;

    static {
        try {
            qol$handlerField = StorageBusPart.class.getDeclaredField("handler");
            qol$handlerField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Unique
    public MEStorage qol$getHandler() throws RuntimeException {
        try {
            return (MEStorage) qol$handlerField.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public StorageBusPart$AddSticky$Mixin(IPartItem<?> partItem) {
        super(partItem);
    }

    @Inject(method = "updateTarget", at = @At(value = "INVOKE", target = "Lappeng/parts/storagebus/StorageBusPart$StorageBusInventory;setVoidOverflow(Z)V"))
    private void detectStickyUpgrade(boolean forceFullUpdate, CallbackInfo ci) {
        if (!StickyCardItem.shouldAct()) return;
        ((ISticky) this.qol$getHandler()).setSticky(this.isUpgradedWith(QoLItems.STICKY_CARD_ITEM));
    }
}
