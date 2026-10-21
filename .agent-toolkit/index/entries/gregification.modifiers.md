+++
title = "GregificationModifiers: \"FE overclock\" (same energy, full voltage, subtick parallels), batch, coil temperature"
keys = [ "allgregified", "batch mode", "coil_temperature", "ebf_temp", "gregification core" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "public static boolean allGregified(GTRecipeType... recipeTypes) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GregificationModifiers.java"
snippet = "public static final RecipeModifier OVERCLOCK = GregificationModifiers::overclock;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GregificationModifiers.java"
snippet = "public static final RecipeModifier BATCH = GregificationModifiers::batch;"
+++
Recipes are converted at a fixed VA[LV] keeping the foreign energy/time where the source has one, so OVERCLOCK only trades time for EU/t. Multiblocks must use gtceu's own GTRecipeModifiers.BATCH_MODE object: the machine UI only shows the batch button for that exact instance. COIL_TEMPERATURE is for recipes with ebf_temp (no source sets it on 1.20.1; MI on 1.21 does). ModularMachines uses OVERCLOCK when allGregified(recipeTypes).