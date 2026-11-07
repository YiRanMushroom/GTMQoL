+++
title = "Wireless part recipes (magical assembler): GT hatch + circuit 5 -> wireless hatch -> covers -> accessor; channel parts"
keys = [ "steam", "wireless.energy", "wireless.steam" ]
updated = "2026-11-07 09:58:45"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/WirelessRecipes.java"
snippet = "public static void addEnergyRecipes(Consumer<FinishedRecipe> provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/WirelessRecipes.java"
snippet = "public static void addSteamRecipes(Consumer<FinishedRecipe> provider) {"
+++
Gated by wireless.energy / wireless.fe (with energy) / wireless.steam / wireless.computation / wireless.opticalData. FE (wireless.fe): our LV wireless energy hatches + circuit 6 -> FE hatches; accessor + both hatches -> energy cube (the AE2 energy cell's recipe is in WirelessEnergyCellMachine). Channel parts (wireless.channels): gtceu's computation/optical data hatch or network switch + circuit 5. Steam starts from GT's steam hatch, circuit 6 for the output side (GT has no steam output hatch).