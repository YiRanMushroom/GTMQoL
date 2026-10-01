package com.yiran.minecraft.gtmqol.predicates;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicateUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class JsonTextNBTPredicate extends NBTPredicate {
    public static final String TYPE = "json_text";
    private final String key;
    private final String expectedText;

    public JsonTextNBTPredicate(String key, String expectedText) {
        this.key = key;
        this.expectedText = expectedText;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean test(CompoundTag tag) {
        Tag toCompare = NBTPredicateUtils.getNestedTag(tag, this.key);

        if (!(toCompare instanceof StringTag stringTag)) {
            return false;
        }

        String rawJson = stringTag.getAsString();

        try {
            Component component = Component.Serializer.fromJson(rawJson);

            if (component != null) {
                return component.getString().equals(this.expectedText);
            }
        } catch (Exception e) {
            return rawJson.equals(this.expectedText);
        }

        return false;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = super.toJson();
        object.addProperty("key", this.key);
        object.addProperty("expectedText", this.expectedText);
        return object;
    }

    public static NBTPredicate fromJson(JsonObject json) {
        if (json.has("key") && json.has("expectedText")) {
            String key = json.get("key").getAsString();
            String expectedText = json.get("expectedText").getAsString();
            return new JsonTextNBTPredicate(key, expectedText);
        } else {
            throw new IllegalStateException("Could not deserialize JsonTextNBTPredicate: " + json);
        }
    }

    @Override
    public String toString() {
        return "JsonTextNBTPredicate{key='" + this.key + "', expectedText='" + this.expectedText + "'}";
    }
}