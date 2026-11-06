package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import brachy.modularui.integration.recipeviewer.handlers.RecipeTransferError;
import brachy.modularui.integration.recipeviewer.handlers.RecipeTransferHandler;
import brachy.modularui.theme.ThemeAPI;
import org.jetbrains.annotations.Nullable;

/**
 * The encoding window. JEI's transfer button reaches it through ModularUI ({@link RecipeTransferHandler}), EMI's
 * through {@link PatternCrafterEmi}.
 */
@OnlyIn(Dist.CLIENT)
final class CraftingPatternScreen extends GTGuiScreen implements RecipeTransferHandler<RecipeHolder<?>> {

    private final CraftingPatternBehavior.EncodingPanel panel;

    CraftingPatternScreen(CraftingPatternBehavior.EncodingPanel panel) {
        // the theme machines use: ModularUI's theme reload leaves gtceu:standard, the default here, without the
        // backgrounds of slots and buttons that are off
        super(GTCEu.MOD_ID, panel, ThemeAPI.DEFAULT_ID);
        this.panel = panel;
    }

    void fill(RecipeHolder<?> recipe) {
        panel.fill(recipe);
    }

    @Override
    public @Nullable RecipeTransferError transferRecipe(RecipeHolder<?> recipe, boolean maxTransfer, boolean simulate) {
        if (CraftingPattern.Mode.of(recipe.value()) == null) return RecipeTransferError.Internal.INSTANCE;
        if (!simulate) fill(recipe);
        return null;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Class<RecipeHolder<?>> getRecipeClass() {
        return (Class) RecipeHolder.class;
    }
}
