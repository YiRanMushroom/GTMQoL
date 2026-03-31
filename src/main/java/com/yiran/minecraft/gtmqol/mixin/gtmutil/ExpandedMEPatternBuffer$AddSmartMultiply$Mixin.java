package com.yiran.minecraft.gtmqol.mixin.gtmutil;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.crafting.pattern.AEProcessingPattern;
import com.extendedae_plus.api.crafting.ScaledProcessingPattern;
import com.extendedae_plus.api.smartDoubling.ISmartDoublingAwarePattern;
import com.extendedae_plus.api.smartDoubling.ISmartDoublingHolder;
import com.google.common.collect.BiMap;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.yiran.minecraft.gtmqol.common.configurator.SmartMultiplierConfigurator;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.extendedae_plus.util.smartDoubling.PatternScaler.getComputedMul;

@Restriction(require = {
        @Condition(value = "extendedae_plus", type = Condition.Type.MOD),
        @Condition(value = "gtmutils", type = Condition.Type.MOD)
})
@Mixin(value = {MEPatternBufferPartMachine.class})
public abstract class ExpandedMEPatternBuffer$AddSmartMultiply$Mixin implements IGridConnectedMachine, ISmartDoublingHolder, ICraftingProvider {
    @Shadow
    @Final
    private BiMap<IPatternDetails, MEPatternBufferPartMachine.InternalSlot> detailsSlotMap;
    @Unique
    @DescSynced
    @Persisted(key = "doSmartMultiply")
    private boolean qol$doSmartMultiply = true;

    @Unique
    @DescSynced
    @Persisted(key = "smartMultiplyLimit")
    private int qol$smartMultiplyLimit;

    @Unique
    private boolean qol$patternIsDirty = true;

    @Inject(method = "attachConfigurators", at = @At("TAIL"))
    private void onAttachConfigurator(ConfiguratorPanel configuratorPanel, CallbackInfo ci) {
        configuratorPanel.attachConfigurators(new SmartMultiplierConfigurator(this));
    }

    @Override
    public boolean eap$getSmartDoubling() {
        return qol$doSmartMultiply;
    }

    @Override
    public void eap$setSmartDoubling(boolean b) {
        this.qol$doSmartMultiply = b;
        this.qol$patternIsDirty = true;
    }

    @Override
    public int eap$getProviderSmartDoublingLimit() {
        return qol$smartMultiplyLimit;
    }

    @Override
    public void eap$setProviderSmartDoublingLimit(int i) {
        this.qol$smartMultiplyLimit = i;
        this.qol$patternIsDirty = true;
    }

    @Unique
    private void qol$updatePatterns() {
        if (qol$patternIsDirty) {
            this.detailsSlotMap.keySet().forEach(pattern -> {
                if (pattern instanceof AEProcessingPattern proc && proc instanceof ISmartDoublingAwarePattern smartDoublingAwarePattern) {
                    smartDoublingAwarePattern.eap$setAllowScaling(qol$doSmartMultiply);
                    smartDoublingAwarePattern.eap$setMultiplierLimit(getComputedMul(proc, qol$smartMultiplyLimit));
                }
            });
            qol$patternIsDirty = false;
        }
    }

    @Definition(id = "needPatternSync", field = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine;needPatternSync:Z")
    @Expression("this.needPatternSync = true")
    @Inject(method = "onPatternChange", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public void alsoMakeDirty(CallbackInfo ci) {
        this.qol$patternIsDirty = true;
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        qol$updatePatterns();
    }

    @Definition(id = "detailsSlotMap", field = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine;detailsSlotMap:Lcom/google/common/collect/BiMap;")
    @Definition(id = "containsKey", method = "Lcom/google/common/collect/BiMap;containsKey(Ljava/lang/Object;)Z")
    @Definition(id = "patternDetails", local = @Local(type = IPatternDetails.class, argsOnly = true))
    @Expression("this.detailsSlotMap.containsKey(patternDetails)")
    @WrapOperation(method = "pushPattern", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean orMultipliedDetail(BiMap<IPatternDetails, MEPatternBufferPartMachine.InternalSlot> instance, Object o, Operation<Boolean> original, @Local(argsOnly = true) IPatternDetails patternDetails) {
        if (original.call(instance, o)) {
            return true;
        }

        if (patternDetails instanceof ScaledProcessingPattern scaledProcessingPattern) {
            return original.call(instance, scaledProcessingPattern.getOriginal());
        }

        return false;
    }

    @Definition(id = "detailsSlotMap", field = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine;detailsSlotMap:Lcom/google/common/collect/BiMap;")
    @Definition(id = "get", method = "Lcom/google/common/collect/BiMap;get(Ljava/lang/Object;)Ljava/lang/Object;")
    @Definition(id = "patternDetails", local = @Local(type = IPatternDetails.class, argsOnly = true))
    @Expression("this.detailsSlotMap.get(@(patternDetails))")
    @ModifyExpressionValue(method = "pushPattern", at = @At("MIXINEXTRAS:EXPRESSION"))
    private IPatternDetails findOriginalPatternSlot(IPatternDetails original) {
        if (original instanceof ScaledProcessingPattern scaledProcessingPattern) {
            return scaledProcessingPattern.getOriginal();
        }

        return original;
    }
}

