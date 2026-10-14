package com.yiran.minecraft.gtmqol.core.mixins.gtceufix.jei;

import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/**
 * Puts back the two fields ModularUI's {@code RecipeSlotAccessor} targets, so its accessor applies and
 * {@code RecipeSlot} loads again on JEI 19.46+ (gated by {@code JeiRecipeSlotFixPlugin}). Priority 500 so this runs
 * before ModularUI's accessor (default 1000). The fields are never read by JEI; ModularUI only writes them for its
 * own JEI categories, which aren't used with EMI. The names must stay as they are (no {@code @Unique}).
 */
@Mixin(targets = "mezz.jei.library.gui.ingredients.RecipeSlot", remap = false, priority = 500)
public class JeiRecipeSlotMixin {

    private List<?> allIngredients;
    private List<?> displayIngredients;
}
