package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.yiran.minecraft.gtmqol.data.QoLItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GTItems.class)
public class GTItems$RegisterMine$Mixin {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onStaticInit(CallbackInfo ci) {
        QoLItems.init();
    }
}
