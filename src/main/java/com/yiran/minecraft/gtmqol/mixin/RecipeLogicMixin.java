package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.yiran.minecraft.gtmqol.mixin_helper.RecipeLogicLimiterData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeLogic.class)
public class RecipeLogicMixin {
//    @Shadow
//    @Final
//    public IRecipeLogicMachine machine;
//
//    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
//    public void serverTick(CallbackInfo ci) {
//        if (!RecipeLogicLimiterData.shouldTick(this.machine)) {
//            ci.cancel();
//        }
//    }
}
