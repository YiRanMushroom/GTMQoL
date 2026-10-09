package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;

import net.minecraft.world.item.crafting.RecipeHolder;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets {@code GTRecipeType.toGTRecipe} return null for a proxied recipe it can't convert (gregified recipe types
 * leave out e.g. MI recipes with process conditions); GTCEu would add the null to the lookup and crash.
 */
@Mixin(value = RecipeManagerHandler.class, remap = false)
public class RecipeManagerHandlerMixin {

    // the lambda in addProxyRecipesToLookup
    @Inject(method = "lambda$addProxyRecipesToLookup$0",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"),
            cancellable = true)
    private static void gtmqol$skipUnconverted(CallbackInfo ci, @Local(ordinal = 1) RecipeHolder<?> gtRecipe) {
        if (gtRecipe == null) ci.cancel();
    }
}
