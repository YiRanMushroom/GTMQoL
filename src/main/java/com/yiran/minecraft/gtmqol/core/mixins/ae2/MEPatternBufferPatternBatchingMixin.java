package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.patternbuffer.PatternBatching;
import com.yiran.minecraft.gtmqol.integration.ae2.crafting.PatternBatchProvider;
import com.yiran.minecraft.gtmqol.integration.ae2.crafting.PatternBatch;

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
 * Pattern batching for GTCEu's pattern buffer (and our larger ones, which extend it), see {@link PatternBatchProvider}.
 * Pushes of batches are matched against the slot patterns by their original, and bring the batch's inputs.
 */
@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class MEPatternBufferPatternBatchingMixin implements PatternBatchProvider {

    @Unique
    @SaveField(nbtKey = "gtmqol$smartDoubling")
    @SyncToClient
    private boolean gtmqol$patternBatching = true;

    // 0 = no limit
    @Unique
    @SaveField(nbtKey = "gtmqol$smartDoublingLimit")
    @SyncToClient
    private int gtmqol$patternBatchLimit = 0;

    // The pattern AE2 passed to pushPattern, possibly a batch, while the method runs on its original.
    @Unique
    private @Nullable IPatternDetails gtmqol$pushedPattern;

    @Unique
    private boolean gtmqol$getPatternBatching() {
        return gtmqol$patternBatching;
    }

    @Unique
    private void gtmqol$setPatternBatching(boolean enabled) {
        gtmqol$patternBatching = enabled;
        gtmqol$self().getSyncDataHolder().markClientSyncFieldDirty("gtmqol$patternBatching");
    }

    @Unique
    private int gtmqol$getPatternBatchLimit() {
        return gtmqol$patternBatchLimit;
    }

    @Unique
    private void gtmqol$setPatternBatchLimit(int limit) {
        gtmqol$patternBatchLimit = Math.max(0, limit);
        gtmqol$self().getSyncDataHolder().markClientSyncFieldDirty("gtmqol$patternBatchLimit");
    }

    @Override
    public long batchLimit(IPatternDetails pattern) {
        if (!gtmqol$patternBatching || !(pattern instanceof AEProcessingPattern)) return 0;
        return PatternBatching.limit(pattern, gtmqol$patternBatchLimit);
    }

    @Unique
    private MEPatternBufferPartMachine gtmqol$self() {
        return (MEPatternBufferPartMachine) (Object) this;
    }

    @ModifyVariable(method = "pushPattern", at = @At("HEAD"), argsOnly = true)
    private IPatternDetails gtmqol$unwrapPatternBatch(IPatternDetails patternDetails) {
        gtmqol$pushedPattern = patternDetails;
        return PatternBatch.unwrap(patternDetails);
    }

    // Push the batch's inputs, not the original's.
    @ModifyArg(method = "pushPattern",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine$InternalSlot;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)V"),
               index = 0,
               require = 2)
    private IPatternDetails gtmqol$pushPatternBatch(IPatternDetails patternDetails) {
        return gtmqol$pushedPattern != null ? gtmqol$pushedPattern : patternDetails;
    }

    @Inject(method = "pushPattern", at = @At("RETURN"))
    private void gtmqol$afterPushPattern(CallbackInfoReturnable<Boolean> cir) {
        gtmqol$pushedPattern = null;
    }

    @ModifyReturnValue(method = "getPanelBuilder", at = @At("RETURN"))
    private MachineUIPanelBuilder gtmqol$addPatternBatchingConfigurator(MachineUIPanelBuilder builder,
                                                                      @Local(argsOnly = true) PanelSyncManager syncManager) {
        return PatternBatching.addConfigurator(builder, syncManager, this::gtmqol$getPatternBatching,
                this::gtmqol$setPatternBatching, this::gtmqol$getPatternBatchLimit, this::gtmqol$setPatternBatchLimit);
    }
}
