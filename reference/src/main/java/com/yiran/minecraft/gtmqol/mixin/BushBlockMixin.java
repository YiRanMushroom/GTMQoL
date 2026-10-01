package com.yiran.minecraft.gtmqol.mixin;

import com.yiran.minecraft.gtmqol.logic.GreenhouseRecipeLogic;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SaplingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BushBlock.class)
public abstract class BushBlockMixin implements ItemLike {
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        BushBlock self = (BushBlock)(Object)this;
        if (self instanceof SaplingBlock) {
            GreenhouseRecipeLogic.addSaplingProvider(self::asItem);
        } else {
            GreenhouseRecipeLogic.addCropBlockProvider(self::asItem);
        }
    }
}
