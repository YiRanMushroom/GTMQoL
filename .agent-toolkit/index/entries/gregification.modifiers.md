+++
title = "GregificationModifiers: \"FE overclock\" (same energy, full voltage, subtick parallels), batch, coil temperature"
keys = [ "allgregified", "batch mode", "coil_temperature", "ebf_temp", "gregification core" ]
updated = "2026-10-21 08:25:36"
checked = "2026-10-21"

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
Fixed voltage per source (Mek LV; Create LV/MV/HV by basin heat; PB LV, incubator ULV), keeping the foreign time, so OVERCLOCK only trades time for EU/t. Create fan: ULV, OC_NON_PERFECT, unlimited AbsoluteParallel; SA line: STEP_LIMIT + OVERCLOCK + BATCH_MODE. Multiblocks must use gtceu's own GTRecipeModifiers.BATCH_MODE (the UI shows the batch button only for it). COIL_TEMPERATURE: ebf_temp recipes (none on 1.20.1). ModularMachines uses OVERCLOCK when allGregified.