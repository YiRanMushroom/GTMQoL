package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.yiran.minecraft.gtmqol.data.QoLMultiblocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GTMultiMachines.class)
public class GTMultiMachines$InjectMyMultiRegistration$Mixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerMyMulties(CallbackInfo ci) {
        QoLMultiblocks.init();
    }
}
