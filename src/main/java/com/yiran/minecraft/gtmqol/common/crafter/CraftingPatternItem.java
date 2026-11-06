package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;

/** A blank or encoded pattern. Here only because the screen comes from the item, not from its behaviors. */
public final class CraftingPatternItem extends ComponentItem {

    public CraftingPatternItem(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ModularScreen createScreen(PlayerInventoryGuiData<?> data, ModularPanel<?> mainPanel) {
        return new CraftingPatternScreen((CraftingPatternBehavior.EncodingPanel) mainPanel);
    }
}
