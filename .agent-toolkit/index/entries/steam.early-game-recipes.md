+++
title = "Early game: casing + chest/glass buses and hatches, any sapling -> rubber sapling"
keys = [ "recipes.earlygame", "steam tweaks", "ulv" ]
updated = "2026-11-17 01:42:57"
checked = "2026-11-17"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/EarlyGameRecipes.java"
snippet = "public final class EarlyGameRecipes {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "RECIPE_TYPE.recipeBuilder(GTMQoL.id(\"rubber_sapling\"))"
+++
Sand -> glass smelting for the glass buses before the alloy smelter is kept by recipes.keepVanillaRecipes (recipes.keep-vanilla-recipes). The rubber sapling recipe (magical assembler: any sapling + sticky resin) is always on with the magical assembler.