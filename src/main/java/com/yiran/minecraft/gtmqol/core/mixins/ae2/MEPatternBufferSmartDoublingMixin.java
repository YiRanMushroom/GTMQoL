package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.SmartDoubling;
import com.yiran.minecraft.gtmqol.integration.ae2.batch.BatchCraftingProvider;
import com.yiran.minecraft.gtmqol.integration.ae2.batch.ScaledPattern;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.pattern.AEProcessingPattern;
import brachy.modularui.value.sync.PanelSyncManager;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Smart doubling for GTCEu's pattern buffer (and our larger ones, which extend it), see {@link BatchCraftingProvider}.
 * Pushes of batches are matched against the slot patterns by their original, and bring the batch's inputs.
 */
@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class MEPatternBufferSmartDoublingMixin implements BatchCraftingProvider {

    @Unique
    @SaveField
    @SyncToClient
    private boolean gtmqol$smartDoubling = true;

    // 0 = no limit
    @Unique
    @SaveField
    @SyncToClient
    private int gtmqol$smartDoublingLimit = 0;

    // The pattern AE2 passed to pushPattern, possibly a batch, while the method runs on its original.
    @Unique
    private @Nullable IPatternDetails gtmqol$pushedPattern;

    @Unique
    private boolean gtmqol$getSmartDoubling() {
        return gtmqol$smartDoubling;
    }

    @Unique
    private void gtmqol$setSmartDoubling(boolean enabled) {
        gtmqol$smartDoubling = enabled;
        gtmqol$self().getSyncDataHolder().markClientSyncFieldDirty("gtmqol$smartDoubling");
    }

    @Unique
    private int gtmqol$getSmartDoublingLimit() {
        return gtmqol$smartDoublingLimit;
    }

    @Unique
    private void gtmqol$setSmartDoublingLimit(int limit) {
        gtmqol$smartDoublingLimit = Math.max(0, limit);
        gtmqol$self().getSyncDataHolder().markClientSyncFieldDirty("gtmqol$smartDoublingLimit");
    }

    @Override
    public long batchLimit(IPatternDetails pattern) {
        if (!gtmqol$smartDoubling || !(pattern instanceof AEProcessingPattern)) return 0;
        return SmartDoubling.limit(pattern, gtmqol$smartDoublingLimit);
    }

    @Unique
    private MEPatternBufferPartMachine gtmqol$self() {
        return (MEPatternBufferPartMachine) (Object) this;
    }

    @ModifyVariable(method = "pushPattern", at = @At("HEAD"), argsOnly = true)
    private IPatternDetails gtmqol$unwrapScaledPattern(IPatternDetails patternDetails) {
        gtmqol$pushedPattern = patternDetails;
        return ScaledPattern.unwrap(patternDetails);
    }

    // Push the batch's inputs, not the original's.
    @ModifyArg(method = "pushPattern",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine$InternalSlot;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)V"),
               index = 0,
               require = 2)
    private IPatternDetails gtmqol$pushScaledPattern(IPatternDetails patternDetails) {
        return gtmqol$pushedPattern != null ? gtmqol$pushedPattern : patternDetails;
    }

    @Inject(method = "pushPattern", at = @At("RETURN"))
    private void gtmqol$afterPushPattern(CallbackInfoReturnable<Boolean> cir) {
        gtmqol$pushedPattern = null;
    }

    @ModifyReturnValue(method = "getPanelBuilder", at = @At("RETURN"))
    private MachineUIPanelBuilder gtmqol$addSmartDoublingConfigurator(MachineUIPanelBuilder builder,
                                                                      @Local(argsOnly = true) PanelSyncManager syncManager) {
        return SmartDoubling.addConfigurator(builder, syncManager, this::gtmqol$getSmartDoubling,
                this::gtmqol$setSmartDoubling, this::gtmqol$getSmartDoublingLimit, this::gtmqol$setSmartDoublingLimit);
    }
}
