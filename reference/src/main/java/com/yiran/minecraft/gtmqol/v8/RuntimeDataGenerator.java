package com.yiran.minecraft.gtmqol.v8;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Runtime generation entry point for v8 content.
 *
 * <p>Registry entries still have to be created during the normal mod lifecycle.
 * This class only owns the resource and data representations that are needed
 * after registration.</p>
 */
public final class RuntimeDataGenerator {
    private static Consumer<FinishedRecipe> recipeSink;

    private RuntimeDataGenerator() {
    }

    static void begin(Consumer<FinishedRecipe> provider) {
        recipeSink = Objects.requireNonNull(provider, "provider");
    }

    public static void addRecipe(FinishedRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (recipeSink == null) {
            throw new IllegalStateException("Recipes can only be added during GTCEu recipe generation");
        }
        recipeSink.accept(recipe);
    }

    public static void addClientJson(ResourceLocation location, JsonElement json) {
        GTDynamicResourcePack.addResource(location, Objects.requireNonNull(json, "json"));
    }

    public static void addClientLanguage(ResourceLocation namespace, String language, String key, String value) {
        JsonObject json = new JsonObject();
        json.addProperty(key, value);
        addClientJson(
                new ResourceLocation(namespace.getNamespace(), "lang/" + language + ".json"),
                json
        );
    }

    public static void addRuntimeRecipe(FinishedRecipe recipe) {
        GTDynamicDataPack.addRecipe(Objects.requireNonNull(recipe, "recipe"));
    }
}
