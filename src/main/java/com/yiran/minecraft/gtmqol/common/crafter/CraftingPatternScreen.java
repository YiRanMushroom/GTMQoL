package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.theme.ThemeAPI;

/** The encoding window; {@link PatternCrafterEmi} and {@link PatternCrafterJei} fill it. */
@OnlyIn(Dist.CLIENT)
final class CraftingPatternScreen extends GTGuiScreen {

    private final CraftingPatternBehavior.EncodingPanel panel;

    CraftingPatternScreen(CraftingPatternBehavior.EncodingPanel panel) {
        // the theme machines use: ModularUI's theme reload leaves gtceu:standard, the default here, without the
        // backgrounds of slots and buttons that are off
        super(GTCEu.MOD_ID, panel, ThemeAPI.DEFAULT_ID);
        this.panel = panel;
    }

    void fill(Recipe<?> recipe) {
        panel.fill(recipe);
    }
}
