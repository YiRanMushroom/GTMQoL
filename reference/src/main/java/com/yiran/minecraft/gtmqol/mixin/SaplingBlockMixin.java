package com.yiran.minecraft.gtmqol.mixin;

import com.yiran.minecraft.gtmqol.logic.GreenhouseRecipeLogic;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.SaplingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin implements ItemLike {

}
