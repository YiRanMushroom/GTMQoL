package com.yiran.minecraft.gtmqol.common.ui;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.yiran.minecraft.gtmqol.common.blockprovider.BlockProviderRecipeViewer;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeViewer;
import com.yiran.minecraft.gtmqol.common.potion.PotionFluidHint;
import com.yiran.minecraft.gtmqol.common.stacklike.StackLikeRecipeViewer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Recipe viewer slots of every GTCEu recipe type for our recipe capabilities (stack-like, block, ghost item), and the
 * any-mod hint on potion fluid inputs. Set once at setup on the built layouts, before any recipe viewer reads them; a
 * recipe type's own settings for these capabilities are kept.
 */
// Untested: recipe viewer pages still show chemical, block and ghost item slots, and potion inputs still flash the
// "any mod" hint
public final class RecipeTypeLayouts {

    private RecipeTypeLayouts() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(RecipeTypeLayouts::apply));
    }

    private static void apply() {
        for (var recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (!(recipeType instanceof GTRecipeType type)) continue;
            var layout = type.getUiLayout();
            StackLikeRecipeViewer.addDefaults(layout);
            BlockProviderRecipeViewer.addDefaults(layout);
            GhostIngredientRecipeViewer.addDefaults(layout);
            PotionFluidHint.addTo(layout);
        }
    }
}
