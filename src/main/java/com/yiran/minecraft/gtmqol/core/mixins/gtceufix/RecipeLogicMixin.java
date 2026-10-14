package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * gtceu bug: when {@code findAndHandleRecipe} restarts the last recipe, it clears {@code lastOriginRecipe} and never
 * sets it back. With {@code alwaysTryModifyRecipe} the next finish then has nothing to re-modify, marks the recipe
 * dirty and goes idle, even though it could go on. So one failed re-check at a finish (e.g. the energy buffer a tick
 * short) costs a second idle a whole recipe later. Keep the origin when the last recipe restarts. Remove once gtceu
 * fixes it.
 */
@Mixin(value = RecipeLogic.class, remap = false)
public abstract class RecipeLogicMixin {

    @Shadow
    @Nullable
    protected GTRecipe lastRecipe;
    @Shadow
    @Nullable
    protected GTRecipe lastOriginRecipe;

    @Inject(method = "findAndHandleRecipe", at = @At("HEAD"))
    private void gtmqol$rememberOrigin(CallbackInfo ci, @Share("origin") LocalRef<GTRecipe> origin) {
        origin.set(lastOriginRecipe);
    }

    @Inject(method = "findAndHandleRecipe", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeLogic;setupRecipe(Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;)V"))
    private void gtmqol$restoreOrigin(CallbackInfo ci, @Share("origin") LocalRef<GTRecipe> origin) {
        if (lastRecipe != null && lastOriginRecipe == null) lastOriginRecipe = origin.get();
    }
}
