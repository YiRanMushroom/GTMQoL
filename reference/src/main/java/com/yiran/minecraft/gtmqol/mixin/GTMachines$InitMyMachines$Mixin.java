package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.common.data.GTMachines;
import com.yiran.minecraft.gtmqol.data.QoLMachines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GTMachines.class)
public class GTMachines$InitMyMachines$Mixin {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void initMyMachines(CallbackInfo ci) {
        QoLMachines.init();
    }
}
