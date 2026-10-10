package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.yiran.minecraft.gtmqol.gregification.GregifiedRecipeType;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

/**
 * Gregified recipe types add their proxied recipes themselves: GTCEu takes exactly one GT recipe per foreign recipe,
 * while a gregified type may make none (e.g. MI recipes with process conditions) or several (e.g. both directions
 * of a Mekanism rotary recipe). Same steps as GTCEu otherwise; other recipe types are left alone.
 */
@Mixin(value = RecipeManagerHandler.class, remap = false)
public class RecipeManagerHandlerMixin {

    @Inject(method = "addProxyRecipesToLookup", at = @At("HEAD"), cancellable = true)
    private static void gtmqol$addGregified(Collection<RecipeHolder<?>> recipes, GTRecipeType gtRecipeType,
                                            RecipeType<?> proxyType, List<RecipeHolder<GTRecipe>> proxyRecipes,
                                            CallbackInfo ci) {
        if (!(gtRecipeType instanceof GregifiedRecipeType gregified)) return;
        var lookup = gtRecipeType.getAdditionHandler();
        proxyRecipes.clear();
        for (RecipeHolder<?> recipe : recipes) {
            if (recipe.value().getType() != proxyType) continue;
            for (RecipeHolder<GTRecipe> gtRecipe : gregified.toGTRecipes(recipe)) {
                proxyRecipes.add(gtRecipe);
                lookup.addStaging(gtRecipe.value());
            }
        }
        ci.cancel();
    }
}
