package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTRecipeEMICategory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.EmiApi;

/**
 * gtceu 1.21 bug: the recipe type button in machine UIs calls the private {@code EmiApi.setPages} through
 * {@code EmiApiAccessor}, whose invoker never gets applied to {@code EmiApi}, so the click throws
 * {@code NoSuchMethodError}. Opens the machine's category through EMI's public API instead; unlike gtceu's
 * version, the other categories of the recipe type aren't shown as tabs. Remove once gtceu fixes it.
 */
@Mixin(targets = "com.gregtechceu.gtceu.utils.GTUtil$EmiCallWrapper", remap = false)
public class EmiCallWrapperMixin {

    @Inject(method = "openRecipeCategory", at = @At("HEAD"), cancellable = true)
    private static void gtmqol$openWithPublicApi(GTRecipeCategory category, CallbackInfo ci) {
        EmiApi.displayRecipeCategory(GTRecipeEMICategory.machineCategory(category));
        ci.cancel();
    }
}
