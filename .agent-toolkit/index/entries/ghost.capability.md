+++
title = "Ghost ingredients: GhostIngredientRecipeCapability, a never-consumed input only a mark satisfies, to pick a recipe"
keys = [ "gtmqol:ghost_ingredient", "recipe search filter", "recipe selection", "setmaxsize" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientRecipeCapability.java"
snippet = "public final class GhostIngredientRecipeCapability extends RecipeCapability<GhostIngredient<?>> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredients.java"
snippet = "public static void init() {"
+++
Both branches; a small API designed by the user. Tells a machine which recipe to run when the real inputs fit several (e.g. Create's saw: planks or stairs from one log). Recipes add .input(CAP, new ItemGhostIngredient(stack)); a recipe type that takes them needs setMaxSize(IO.IN, CAP, n). Lookup: ghost.lookup; distinctness: ghost.distinct.