package com.yiran.minecraft.gtmqol.mixin.modpack_integration.monifactory;

import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import com.yiran.minecraft.gtmqol.integration.KJSInjector;
import com.yiran.minecraft.gtmqol.integration.monifactory.MoniRecipeTypesExtension;
import com.yiran.minecraft.gtmqol.integration.monifactory.MoniUtils;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Condition(type = Condition.Type.MOD, value = "monilabs")
@Mixin(KJSInjector.class)
public class KJSInjector$InitMoni$Mixin {
    @Inject(method = "mixinInitInjectionPoint", at = @At("TAIL"))
    private static void gtmqol$initMoniFactoryIntegration(CallbackInfo ci) {
        if (MoniUtils.isMoniFactoryAndIntegrationEnabled()) {
            KJSInjector.injectStartupScript("kjs/modpack_integration/monifactory/startup_scripts/moni_startup.js", "monifactory/moni_startup.js");
            KJSInjector.injectClientScript("kjs/modpack_integration/monifactory/client_scripts/moni_client.js", "monifactory/moni_client.js");
            KJSInjector.injectServerScript("kjs/modpack_integration/monifactory/server_scripts/moni_server.js", "monifactory/moni_server.js");
        }
    }
}
