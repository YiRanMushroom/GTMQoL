package com.yiran.minecraft.gtmqol.gregification.client;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.yiran.minecraft.gtmqol.gregification.Gregification;
import com.yiran.minecraft.gtmqol.gregification.GregifiedRecipeType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GTCEu converts proxied recipes only where recipes are loaded, i.e. on the server, and the converted recipes are
 * not synced (they aren't in the recipe manager). In singleplayer the client shares the server's recipe types, but
 * on a dedicated server the recipe viewer would show empty categories. So the client converts the synced foreign
 * recipes itself and puts them in the categories the recipe viewer reads. Joining a server sends the recipes before
 * the tags, so this is done again once the tags arrive, for converters reading tags (the Create fan).
 */
public final class GregificationClient {

    private static final Map<GregifiedRecipeType, List<GTRecipe>> ADDED = new HashMap<>();

    private GregificationClient() {}

    public static void init() {
        // before the recipe viewers reload
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, GregificationClient::onRecipesUpdated);
        // after LoadingTags drops what it worked out from the old tags
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, GregificationClient::onTagsUpdated);
    }

    /** The client's main thread. */
    public static BlockableEventLoop<?> executor() {
        return Minecraft.getInstance();
    }

    /** For recipe logic in common code, which must not load client classes itself. */
    public static @Nullable Level level() {
        return Minecraft.getInstance().level;
    }

    private static void onRecipesUpdated(RecipesUpdatedEvent event) {
        convert(event.getRecipeManager());
    }

    private static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) return;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) convert(connection.getRecipeManager());
    }

    private static void convert(RecipeManager manager) {
        // the integrated server already filled the same recipe types
        if (Minecraft.getInstance().getSingleplayerServer() != null) return;
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
                for (Recipe<?> recipe : manager.getRecipes()) {
                    if (recipe.getType() != proxy) continue;
                    for (GTRecipe converted : type.toGTRecipes(recipe)) {
                        type.addToCategoryMap(converted.recipeCategory, converted);
                        added.add(converted);
                    }
                }
            }
            ADDED.put(type, added);
        }
    }
}
