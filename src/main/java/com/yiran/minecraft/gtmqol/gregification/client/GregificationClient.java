package com.yiran.minecraft.gtmqol.gregification.client;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.yiran.minecraft.gtmqol.gregification.Gregification;
import com.yiran.minecraft.gtmqol.gregification.GregifiedRecipeType;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GTCEu converts proxied recipes only where recipes are loaded, i.e. on the server, and the converted recipes are
 * not synced (they aren't in the recipe manager). In singleplayer the client shares the server's recipe types, but
 * on a dedicated server the recipe viewer would show empty categories. So the client converts the synced foreign
 * recipes itself and puts them in the categories the recipe viewer reads.
 */
public final class GregificationClient {

    private static final Map<GregifiedRecipeType, List<GTRecipe>> ADDED = new HashMap<>();

    private GregificationClient() {}

    public static void init() {
        // before the recipe viewers reload
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RecipesUpdatedEvent.class,
                GregificationClient::onRecipesUpdated);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void onRecipesUpdated(RecipesUpdatedEvent event) {
        // the integrated server already filled the same recipe types
        if (Minecraft.getInstance().getSingleplayerServer() != null) return;
        RecipeManager manager = event.getRecipeManager();
        for (GregifiedRecipeType type : Gregification.recipeTypes()) {
            List<GTRecipe> previous = ADDED.remove(type);
            if (previous != null) {
                for (GTRecipe recipe : previous) {
                    Set<GTRecipe> category = type.getCategoryMap().get(recipe.recipeCategory);
                    if (category != null) category.remove(recipe);
                }
            }
            List<GTRecipe> added = new ArrayList<>();
            for (RecipeType<?> proxy : type.getProxyRecipes().keySet()) {
                List<RecipeHolder<?>> holders = (List) manager.getAllRecipesFor((RecipeType) proxy);
                for (RecipeHolder<?> holder : holders) {
                    RecipeHolder<GTRecipe> converted = type.toGTRecipe(holder);
                    if (converted == null) continue;
                    type.addToCategoryMap(converted.value().recipeCategory, converted.value());
                    added.add(converted.value());
                }
            }
            ADDED.put(type, added);
        }
    }
}
