+++
title = "GregificationModifiers: \"FE overclock\" (same energy, full voltage, subtick parallels), batch, coil temperature"
keys = [ "batch mode", "coil_temperature", "ebf_temp", "gregification core" ]
updated = "2026-10-21 08:11:19"
checked = "2026-10-21"

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
Recipes are converted at a fixed voltage (VA[LV] for MI/Mek/PB; Create LV/MV/HV by heat, fan ULV with non-perfect OC) keeping the foreign total energy where the source has one, so OVERCLOCK only trades time for EU/t. Multiblocks must use gtceu's own GTRecipeModifiers.BATCH_MODE object: MachineUIPanelBuilder only shows the batch button for that exact instance. ModularMachines uses OVERCLOCK when allGregified(recipeTypes).