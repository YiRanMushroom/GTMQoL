package com.yiran.minecraft.gtmqol;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The only generation API used by new content.
 *
 * <p>Registries are still populated during normal mod initialization. This
 * class only publishes the JSON representation needed by the client or by
 * GTCEu's runtime data pack.</p>
 */
public final class RuntimeGeneration {
    private static Consumer<FinishedRecipe> recipeProvider;

    private RuntimeGeneration() {
    }

    static void beginRecipes(Consumer<FinishedRecipe> provider) {
        recipeProvider = Objects.requireNonNull(provider, "provider");
    }

    public static void addRecipe(FinishedRecipe recipe) {
        if (recipeProvider == null) {
            throw new IllegalStateException("Recipes are only available during GTCEu recipe collection");
        }
        recipeProvider.accept(Objects.requireNonNull(recipe, "recipe"));
    }

    public static void addClientResource(ResourceLocation location, JsonElement json) {
        GTDynamicResourcePack.addResource(
                Objects.requireNonNull(location, "location"),
                Objects.requireNonNull(json, "json")
        );
    }

    public static void addModel(ResourceLocation location, JsonElement json) {
        GTDynamicResourcePack.addModel(
                Objects.requireNonNull(location, "location"),
                Objects.requireNonNull(json, "json")
        );
    }

    public static void addBlockState(ResourceLocation location, JsonElement json) {
        GTDynamicResourcePack.addBlockState(
                Objects.requireNonNull(location, "location"),
                Objects.requireNonNull(json, "json")
        );
    }

    public static void addLanguage(ResourceLocation location, JsonElement json) {
        addClientResource(
                Objects.requireNonNull(location, "location"),
                Objects.requireNonNull(json, "json")
        );
    }
}
