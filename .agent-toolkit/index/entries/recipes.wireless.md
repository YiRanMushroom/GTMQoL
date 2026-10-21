+++
title = "Wireless part recipes (magical assembler): hatch + circuit 5 -> wireless hatch -> 4 covers -> accessor/monitor"
keys = [ "steam", "wireless.energy", "wireless.steam" ]
updated = "2026-10-19 20:12:38"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/WirelessRecipes.java"
snippet = "public static void addEnergyRecipes(Consumer<FinishedRecipe> provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/WirelessRecipes.java"
snippet = "public static void addSteamRecipes(Consumer<FinishedRecipe> provider) {"
+++
Gated by wireless.energy / wireless.steam. Steam starts from GT's steam hatch, circuit 6 for the output side (GT has no steam output hatch).