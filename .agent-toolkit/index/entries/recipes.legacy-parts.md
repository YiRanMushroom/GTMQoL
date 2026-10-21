+++
title = "Legacy part recipes: rod/bolt/gear/small gear/rotor (+ long rod) extruder molds that GTCEu v8 removed"
keys = [ "materialamount", "recipes.uselegacypartrecipes", "uselegacypartrecipes" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/part/LegacyPartRecipes.java"
snippet = "rotor.materialAmount(M * 4);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/part/LegacyPartRecipes.java"
snippet = "private static void addMoldRecipes(RecipeOutput provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "if (config.recipes.useLegacyPartRecipes) LegacyPartRecipes.addRecipes(provider);"
+++
GTCEu v8 (2026-10-13/14 snapshots, both branches) makes rods on the lathe (25 mB lubricant doubles output), gears in the forming press, rotors in the welder (4 plates + ring). Extruder recipes copied from v7/early v8 MaterialRecipeHandler/PartsRecipeHandler. Rotor back to M*4 (v8: M*17/4) applies to welded rotors too. Recipe ids `gtmqol:extrude_<material>_...` avoid clashes if GTCEu re-adds them. Item models/lang need runData. Default on.