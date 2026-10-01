package com.yiran.minecraft.gtmqol.mixin.extendedae;

import appeng.api.parts.IPartItem;
import appeng.parts.automation.UpgradeablePart;
import com.glodblock.github.extendedae.common.parts.base.PartSpecialStorageBus;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.common.item.StickyCardItem;
import com.yiran.minecraft.gtmqol.data.QoLItems;
import com.yiran.minecraft.gtmqol.integration.ae2.ISticky;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//@Restriction(require = @Condition(type = Condition.Type.MOD, value = "extendedae"))
@Restriction(
        require = @Condition(type = Condition.Type.MOD, value = "expatternprovider")
)
@Mixin(PartSpecialStorageBus.class)
public abstract class PartSpacialStorageBus$AddSticky$Mixin extends UpgradeablePart {
    @Shadow
    @Final
    protected PartSpecialStorageBus.StorageBusInventory handler;

    public PartSpacialStorageBus$AddSticky$Mixin(IPartItem<?> partItem) {
        super(partItem);
    }

    @Inject(method = "updateTarget", at = @At(value = "INVOKE", target = "Lcom/glodblock/github/extendedae/common/parts/base/PartSpecialStorageBus$StorageBusInventory;setVoidOverflow(Z)V"))
    private void upgradeStickyIfNeeded(boolean forceFullUpdate, CallbackInfo ci) {
        if (this.handler instanceof ISticky stickyHandler) {
            if (!StickyCardItem.shouldAct()) return;
            stickyHandler.setSticky(this.isUpgradedWith(QoLItems.STICKY_CARD_ITEM));
        } else {
            GTMQoL.LOGGER.warn("Failed to apply sticky upgrade to PartSpecialStorageBus because the handler is not an instance of ISticky. This is likely caused by a mod conflict. Please report this to the mod author.");
        }
    }
}
