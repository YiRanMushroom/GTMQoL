package com.yiran.minecraft.gtmqol.mixin;

import com.yiran.minecraft.gtmqol.mixin_helper.RecipeLogicLimiterData;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
//    @Inject(method = "tick", at = @At("HEAD"))
//    private void tick(CallbackInfo ci) {
//        RecipeLogicLimiterData.onTickStarted();
//    }
}
