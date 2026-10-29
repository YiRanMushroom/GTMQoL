+++
title = "Wireless part recipes (magical assembler): GT hatch + circuit 5 -> wireless hatch -> covers -> accessor; channel parts"
keys = [ "steam", "wireless.energy", "wireless.steam" ]
updated = "2026-10-29 07:34:06"
checked = "2026-10-29"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/WirelessRecipes.java"
snippet = "public static void addEnergyRecipes(Consumer<FinishedRecipe> provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/WirelessRecipes.java"
snippet = "public static void addSteamRecipes(Consumer<FinishedRecipe> provider) {"
+++
Gated by wireless.energy / wireless.steam / wireless.computation / wireless.opticalData. Channel parts (wireless.channels): gtceu's computation/optical data hatch or network switch + circuit 5. Steam starts from GT's steam hatch, circuit 6 for the output side (GT has no steam output hatch).