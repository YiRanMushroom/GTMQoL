package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.client.ClientProxy;
import com.yiran.minecraft.gtmqol.data.ClientDynamicModelRegisterer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientProxy.class)
public class GTClientProxyMixin {
    @Inject(method = "postRegisterDynamicAssets", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/utils/data/RuntimeBlockstateProvider;run()V", shift = Shift.BEFORE))
    private void runMyDynamicAssetGeneration(CallbackInfo ci) {
        ClientDynamicModelRegisterer.setupClient();
    }
}
