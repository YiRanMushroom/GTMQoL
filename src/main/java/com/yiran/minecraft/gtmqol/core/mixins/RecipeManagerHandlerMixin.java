package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.yiran.minecraft.gtmqol.gregification.GregifiedRecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Gregified recipe types add their proxied recipes themselves: GTCEu takes exactly one GT recipe per foreign recipe,
 * while a gregified type may make none or several (e.g. both directions of a Mekanism rotary recipe). Same steps as
 * GTCEu otherwise; other recipe types are left alone.
 * <p>
 * {@code recipesByID} is only the proxied type's recipes when called from GTCEu's {@code RecipeManagerMixin}, but every
 * recipe when called from its KubeJS plugin, hence the type check.
 */
@Mixin(value = RecipeManagerHandler.class, remap = false)
public class RecipeManagerHandlerMixin {

    @Inject(method = "addProxyRecipesToLookup", at = @At("HEAD"), cancellable = true)
    private static void gtmqol$addGregified(Map<ResourceLocation, Recipe<?>> recipesByID, GTRecipeType gtRecipeType,
                                            RecipeType<?> proxyType, List<GTRecipe> proxyRecipes,
                                            CallbackInfo ci) {
        if (!(gtRecipeType instanceof GregifiedRecipeType gregified)) return;
        var lookup = gtRecipeType.getAdditionHandler();
        proxyRecipes.clear();
        for (Recipe<?> recipe : recipesByID.values()) {
            if (recipe.getType() != proxyType) continue;
            for (GTRecipe gtRecipe : gregified.toGTRecipes(recipe)) {
                proxyRecipes.add(gtRecipe);
                lookup.addStaging(gtRecipe);
            }
        }
        ci.cancel();
    }
}
