package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.api.mui.GTGuiScreen;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** The encoding window; {@link PatternCrafterEmi} and {@link PatternCrafterJei} fill it. */
@OnlyIn(Dist.CLIENT)
final class CraftingPatternScreen extends GTGuiScreen {

    private final CraftingPatternBehavior.EncodingPanel panel;

    CraftingPatternScreen(CraftingPatternBehavior.EncodingPanel panel) {
        super(panel);
        this.panel = panel;
    }

    void fill(Recipe<?> recipe) {
        panel.fill(recipe);
    }
}
