+++
title = "Greenhouse recipe sources: vanilla/GT by hand, then Mystical Agriculture, then other mods guessed by name"
keys = [ "addguessedplants", "cropblock", "essence", "greenhouse", "planted", "saplingblock" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/greenhouse/Greenhouse.java"
snippet = "private static void addGuessedPlants(RecipeOutput provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/mysticalagriculture/MAGreenhouseRecipes.java"
snippet = "public static void addRecipes(RecipeOutput provider) {"
+++
Order matters: guesses run last and skip anything in PLANTED (and the minecraft namespace), so they never add a conflicting second recipe. MA: API only (compileOnly + dev runtime with Cucumber); no tier essences or seeds, those are crafted in MA.