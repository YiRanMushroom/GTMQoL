+++
title = "Wireless part recipes (magical assembler): GT hatch + circuit 5 -> wireless hatch -> covers -> accessor; channel parts"
keys = [ "steam" ]
updated = "2026-11-07 10:17:51"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessRecipes.java"
snippet = "public static void addEnergyRecipes(RecipeOutput provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessRecipes.java"
snippet = "public static void addSteamRecipes(RecipeOutput provider) {"
+++
WirelessRecipes, all in the magical assembler, each gated by its wireless.* toggle (GTMQoLAddon.addRecipes). Energy per tier: GT hatch + circuit 5 -> wireless hatch, wireless hatch + circuit 5 -> covers, GT hatch + covers -> accessor; plus monitor. Steam starts from GT's steam hatch (circuit 6 for the output side). FE (wireless.energy + wireless.fe): from the LV wireless parts with circuit 6. Channel parts (computation transmitter/receiver/router, optical data transmitter/receiver; wireless.computation / wireless.opticalData; see wireless.channels): gtceu's hatch or network switch + circuit 5.