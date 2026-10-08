package com.yiran.minecraft.gtmqol.integration.mysticalagriculture;

import com.yiran.minecraft.gtmqol.common.greenhouse.Greenhouse;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;

/**
 * Greenhouse recipes for every enabled Mystical Agriculture crop, its addons' included. Only loaded when Mystical
 * Agriculture is.
 * <p>
 * Tier 1 (and elemental) takes as long as a vanilla crop, each tier above twice the one below. Every crop gives 2 of
 * its essence; the 1 in 10 fertilized essence is MA's own secondary drop.
 */
public final class MAGreenhouseRecipes {

    private MAGreenhouseRecipes() {}

    public static void addRecipes(RecipeOutput provider) {
        Item fertilized = BuiltInRegistries.ITEM.get(MysticalAgricultureAPI.resource("fertilized_essence"));
        for (Crop crop : MysticalAgricultureAPI.getCropRegistry().getCrops()) {
            if (!crop.isEnabled()) continue;
            Item seeds = crop.getSeedsItem();
            Item essence = crop.getEssenceItem();
            if (seeds == null || essence == null) continue;

            int tier = Math.max(crop.getTier().getValue(), 1);
            Greenhouse.plant(seeds, Greenhouse.CROP_DURATION << (tier - 1))
                    .outputItems(essence, 2)
                    .chancedOutput(fertilized, "1/10")
                    .save(provider);
        }
    }
}
