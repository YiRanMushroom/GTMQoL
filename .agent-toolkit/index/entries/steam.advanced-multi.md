+++
title = "AdvancedSteamMultiMachine: large steam multis with a max recipe tier, electric-style OC, 0.8x time / 0.75x EU, parallels"
keys = [ "advanced_steam_multi", "large steam multis", "maxrecipetier", "non_perfect_overclock", "overclock", "recipemodifier", "steam multiblock", "steamparallelmultiblockmachine" ]
updated = "2026-10-21 08:13:50"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamMultiMachine.java"
snippet = "public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {"
+++
Recipes up to maxRecipeTier are overclocked to that tier with OverclockingLogic.NON_PERFECT_OVERCLOCK: GTCEu's non-perfect OC, or ours with overclocking.overhaul on (x4 EU/t, /4 duration, no ULV penalty). Then 0.8x duration and 0.75x EU/t. Steam = EU x 2 mB (superclass CONVERSION_RATE). Toggle machines.advancedSteamMachines (all steam content as one unit). Parallels: 16, or the parallel hatch found in formStructure (steam.parallel-hatch).