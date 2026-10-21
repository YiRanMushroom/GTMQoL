+++
title = "AdvancedSteamMultiMachine: large steam multis with a max recipe tier, electric-style OC, 0.8x time / 0.75x EU, parallels"
keys = [ "advanced_steam_multi", "large steam multis", "maxrecipetier", "non_perfect_overclock", "overclock", "recipemodifier", "steam multiblock", "steamparallelmultiblockmachine" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamMultiMachine.java"
snippet = "public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {"
+++
The class javadoc's "non-perfect: EU/t x4, duration /2" holds only without the OC overhaul; with overclocking.overhaul on, OverclockingLogicMixin maps NON_PERFECT_OVERCLOCK to Overclocking.STANDARD (x4 EU/t, /4 duration, no ULV penalty). Steam = EU x 2 mB (superclass). Toggle machines.advancedSteamMachines (all steam content as one unit). Parallels: 16, or the parallel hatch found in formStructure (steam.parallel-hatch).