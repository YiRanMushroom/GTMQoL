package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.SharedHatches;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** The {@link SharedHatches} toggle of a pattern buffer. */
@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class MEPatternBufferSharedHatchesMixin implements SharedHatches.Holder {

    @Unique
    @SaveField
    @SyncToClient
    private boolean gtmqol$sharedHatches = true;

    @Override
    public boolean gtmqol$sharesHatches() {
        return gtmqol$sharedHatches;
    }

    @Override
    public void gtmqol$setSharesHatches(boolean shares) {
        gtmqol$sharedHatches = shares;
        ((MEPatternBufferPartMachine) (Object) this).getSyncDataHolder()
                .markClientSyncFieldDirty("gtmqol$sharedHatches");
    }

    @ModifyReturnValue(method = "getPanelBuilder", at = @At("RETURN"))
    private MachineUIPanelBuilder gtmqol$addSharedHatchesToggle(MachineUIPanelBuilder builder) {
        return SharedHatches.addToggle(builder, this);
    }
}
