+++
title = "Where GTMQoL's recipes come from: IGTAddon.addRecipes, per-feature addRecipes gated by config"
keys = [ "1.21", "config toggle", "finishedrecipe", "recipe registration", "runtime data pack" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "public void addRecipes(Consumer<FinishedRecipe> provider) {"
+++
GTCEu runs this into its runtime data pack together with its own recipes, so no recipe JSON is datagen'd. Each feature has its own static addRecipes(Consumer<FinishedRecipe>), gated by the same config toggle as the feature (AE2 ones only when AE2 is loaded; sticky card by the early option). 1.21.1 takes a RecipeOutput instead.