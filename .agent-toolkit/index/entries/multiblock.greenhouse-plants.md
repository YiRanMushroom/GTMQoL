+++
title = "Greenhouse recipe sources: vanilla/GT by hand, then Mystical Agriculture, then other mods guessed by name"
keys = [ "addguessedplants", "cropblock", "essence", "greenhouse", "planted", "saplingblock" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/greenhouse/Greenhouse.java"
snippet = "private static void addGuessedPlants(Consumer<FinishedRecipe> provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/mysticalagriculture/MAGreenhouseRecipes.java"
snippet = "public static void addRecipes(Consumer<FinishedRecipe> provider) {"

[[items]]
name = "1.20.1-fertilized-essence-id"
value = "MA 1.20.1 (7.0.24) API has no MysticalAgricultureAPI.resource, so the fertilized essence id is built by hand from MOD_ID."
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/mysticalagriculture/MAGreenhouseRecipes.java"
snippet = "MysticalAgricultureAPI.MOD_ID, \"fertilized_essence\""
+++
Order matters: guesses run last and skip anything in PLANTED (and the minecraft namespace), so they never add a conflicting second recipe. MA: API only (modCompileOnly + modLocalRuntime with Cucumber); no tier essences or seeds, those are crafted in MA.