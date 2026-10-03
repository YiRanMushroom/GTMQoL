package com.yiran.minecraft.gtmqol.core.mixins.eap;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.SmartDoubling;

import appeng.api.crafting.IPatternDetails;
import brachy.modularui.value.sync.PanelSyncManager;
import com.extendedae_plus.api.smartDoubling.ISmartDoublingHolder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ExtendedAE Plus smart doubling for GTCEu's pattern buffer (and our larger ones, which extend it). The
 * settings are copied onto the slot patterns before AE2 reads them, and pushes of scaled patterns are matched
 * against the slot patterns by their original.
 */
@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class MEPatternBufferSmartDoublingMixin implements ISmartDoublingHolder {

    @Shadow
    @Final
    private @Nullable IPatternDetails[] patternSlotDetails;

    @Unique
    @SaveField
    @SyncToClient
    private boolean gtmqol$smartDoubling = true;

    // 0 = no limit
    @Unique
    @SaveField
    @SyncToClient
    private int gtmqol$smartDoublingLimit = 0;

    // The pattern AE2 passed to pushPattern, possibly scaled, while the method runs on its original.
    @Unique
    private @Nullable IPatternDetails gtmqol$pushedPattern;

    @Override
    public boolean eap$getSmartDoubling() {
        return gtmqol$smartDoubling;
    }

    @Override
    public void eap$setSmartDoubling(boolean enabled) {
        gtmqol$smartDoubling = enabled;
        gtmqol$self().getSyncDataHolder().markClientSyncFieldDirty("gtmqol$smartDoubling");
        gtmqol$applySmartDoubling();
    }

    @Override
    public int eap$getProviderSmartDoublingLimit() {
        return gtmqol$smartDoublingLimit;
    }

    @Override
    public void eap$setProviderSmartDoublingLimit(int limit) {
        gtmqol$smartDoublingLimit = Math.max(0, limit);
        gtmqol$self().getSyncDataHolder().markClientSyncFieldDirty("gtmqol$smartDoublingLimit");
        gtmqol$applySmartDoubling();
    }

    @Unique
    private MEPatternBufferPartMachine gtmqol$self() {
        return (MEPatternBufferPartMachine) (Object) this;
    }

    @Unique
    private void gtmqol$applySmartDoubling() {
        SmartDoubling.apply(patternSlotDetails, gtmqol$smartDoubling, gtmqol$smartDoublingLimit);
    }

    // Slot patterns are decoded again on every change, so set them up each time AE2 asks for the list.
    @Inject(method = "getAvailablePatterns", at = @At("HEAD"))
    private void gtmqol$beforeAvailablePatterns(CallbackInfoReturnable<?> cir) {
        gtmqol$applySmartDoubling();
    }

    @ModifyVariable(method = "pushPattern", at = @At("HEAD"), argsOnly = true)
    private IPatternDetails gtmqol$unwrapScaledPattern(IPatternDetails patternDetails) {
        gtmqol$pushedPattern = patternDetails;
        return SmartDoubling.unwrap(patternDetails);
    }

    // Push the scaled inputs, not the original's.
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
        return SmartDoubling.addConfigurator(builder, syncManager, this);
    }
}
