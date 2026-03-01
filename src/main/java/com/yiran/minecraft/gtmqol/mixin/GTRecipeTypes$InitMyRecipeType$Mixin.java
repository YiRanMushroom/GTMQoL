package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.yiran.minecraft.gtmqol.data.QoLRecipeTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GTRecipeTypes.class)
public class GTRecipeTypes$InitMyRecipeType$Mixin {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void initMyRecipeType(CallbackInfo ci) {
        QoLRecipeTypes.init();
    }
}
