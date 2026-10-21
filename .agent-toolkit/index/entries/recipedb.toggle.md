+++
title = "recipeDB.groupedSearch early option: RecipeDBMixinPlugin applies or skips the whole gtmqol.recipedb.mixins.json set"
keys = [ "gtmqol-early.properties" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/EarlyConfig.java"
snippet = "defaults.setProperty(\"recipeDB.groupedSearch\", \"true\");"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/RecipeDBMixinPlugin.java"
snippet = "public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {\n        return EarlyConfig.RECIPE_DB_GROUPED_SEARCH;"

[[links]]
path = "src/main/resources/gtmqol.recipedb.mixins.json"
snippet = "\"plugin\": \"com.yiran.minecraft.gtmqol.core.RecipeDBMixinPlugin\","

[[links]]
path = "build.gradle"
snippet = "config 'gtmqol.recipedb.mixins.json'"
+++
Off = GTCEu's search untouched. Restart required. EarlyConfig writes missing keys with defaults into config/gtmqol-early.properties. Logs `RecipeDB grouped search enabled|disabled` at mixin load. On 1.20.1 the mixin config is registered in build.gradle (no mods.toml [[mixins]]).