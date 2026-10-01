package com.yiran.minecraft.gtmqol.mixin.applied_energistics;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.prioritylist.IPartitionList;
import com.yiran.minecraft.gtmqol.integration.ae2.ISticky;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Restriction(
        require = @Condition(type = Condition.Type.MOD, value = "ae2")
)
@Mixin(MEInventoryHandler.class)
public abstract class MEInventoryHandler$AddSticky$Mixin implements ISticky {
    @Shadow public abstract long insert(AEKey what, long amount, Actionable mode, IActionSource source);

    @Shadow protected abstract boolean passesBlackOrWhitelist(AEKey input);

    @Shadow private IPartitionList partitionList;

    @Override
    public boolean isSticky() {
        return qol$isSticky;
    }

    @Override
    public void setSticky(boolean sticky) {
        qol$isSticky = sticky;
    }

    @Override
    public boolean shouldStick(AEKey key, IActionSource source) {
        return isSticky() && !this.partitionList.isEmpty() && this.passesBlackOrWhitelist(key);
    }

    @Unique
    private boolean qol$isSticky = false;
}
