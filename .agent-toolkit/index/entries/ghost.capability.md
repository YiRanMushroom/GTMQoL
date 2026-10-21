+++
title = "Ghost ingredients: GhostIngredientRecipeCapability, a never-consumed input only a mark satisfies, to pick a recipe"
keys = [ "gtmqol:ghost_ingredient", "recipe search filter", "recipe selection", "setmaxsize" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientRecipeCapability.java"
snippet = "public final class GhostIngredientRecipeCapability extends RecipeCapability<GhostIngredient<?>> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredients.java"
snippet = "public static void init() {"
+++
A small API designed by the user (1.21.1 has the same). Tells a machine which recipe to run when the real inputs fit several (e.g. Create's saw: planks or stairs from one log). Recipes add .input(CAP, new ItemGhostIngredient(stack)); a recipe type that takes them needs setMaxSize(IO.IN, CAP, n). getMaxParallelByInput returns limit so it never limits parallels. Hatch recipe: magical assembler, LV input bus + 2 LV circuits, circuit 11. Lookup: ghost.lookup; distinctness: ghost.distinct.