+++
title = "GregificationModifiers: \"FE overclock\" (same energy, full voltage, subtick parallels), batch, coil temperature"
keys = [ "batch mode", "coil_temperature", "ebf_temp", "gregification core" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GregificationModifiers.java"
snippet = "public static final RecipeModifier OVERCLOCK = GregificationModifiers::overclock;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GregificationModifiers.java"
snippet = "public static final RecipeModifier BATCH = GregificationModifiers::batch;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "public static boolean allGregified(GTRecipeType... recipeTypes) {"
+++
Recipes are converted at a fixed VA[LV] keeping the foreign total energy where the source has one, so OVERCLOCK only trades time for EU/t. Multiblocks must use gtceu's own GTRecipeModifiers.BATCH_MODE object: MachineUIPanelBuilder only shows the batch button for that exact instance. ModularMachines uses OVERCLOCK when allGregified(recipeTypes).