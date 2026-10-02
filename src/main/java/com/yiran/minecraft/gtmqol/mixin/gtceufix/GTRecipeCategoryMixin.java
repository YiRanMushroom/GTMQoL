package com.yiran.minecraft.gtmqol.mixin.gtceufix;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;

import net.minecraft.resources.ResourceLocation;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * gtceu 1.21 bug: a recipe type's default category has the same id as the type, and its name key is
 * {@code recipe_category.<ns>.<path>}, but Registrate only generates {@code recipe_type.<ns>.<path>} (gtceu's own lang
 * has no {@code recipe_category.*} at all), so EMI/JEI show the raw key. Use the recipe type's key for default
 * categories; extra categories (KubeJS, {@code GTRegistrate.recipeCategory}) keep theirs. Remove once gtceu fixes it.
 */
@Mixin(value = GTRecipeCategory.class, remap = false)
public class GTRecipeCategoryMixin {

    @Shadow
    @Final
    public ResourceLocation id;

    @Shadow
    @Final
    private GTRecipeType recipeType;

    @ModifyReturnValue(method = "getLanguageKey", at = @At("RETURN"))
    private String gtmqol$useRecipeTypeKey(String key) {
        return this.id.equals(this.recipeType.id) ? this.id.toLanguageKey(GTRecipeType.LANGUAGE_KEY_PATH) : key;
    }
}
