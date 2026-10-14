package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.yiran.minecraft.gtmqol.common.stacklike.StackLikeRecipeViewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The builder sets up the item/fluid/EU/CWU recipe viewer defaults; add ours for stack-like capabilities. */
@Mixin(value = GTRecipeTypeUILayout.Builder.class, remap = false)
public class GTRecipeTypeUILayoutBuilderMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtmqol$stackLikeDefaults(GTRecipeType recipeType, CallbackInfo ci) {
        StackLikeRecipeViewer.addDefaults((GTRecipeTypeUILayout.Builder) (Object) this);
    }
}
