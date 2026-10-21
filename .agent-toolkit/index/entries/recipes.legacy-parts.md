+++
title = "Legacy part recipes: rod/bolt/gear/small gear/rotor (+ long rod) extruder molds that GTCEu v8 removed"
keys = [ "materialamount", "recipes.uselegacypartrecipes", "registertagprefixes", "uselegacypartrecipes" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "if (config.recipes.useLegacyPartRecipes) LegacyPartRecipes.addRecipes(provider);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "if (GTMQoLConfig.get().recipes.useLegacyPartRecipes) LegacyPartRecipes.restoreRotorAmount();"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/part/LegacyPartRecipes.java"
snippet = "rotor.materialAmount(M * 4);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/part/LegacyPartRecipes.java"
snippet = "private static void addMoldRecipes(Consumer<FinishedRecipe> provider) {"
+++
GTCEu v8 (both branches) makes rods on the lathe, gears in the forming press, rotors in the welder (4 plates + ring). Extruder recipes copied from v7/early v8 MaterialRecipeHandler/PartsRecipeHandler. Rotor back to M*4 (v8: M*17/4) applies to welded rotors too; set in registerTagPrefixes because touching TagPrefix during mod construction registers prefixes too late. Ids gtmqol:extrude_<material>_... avoid clashes. Mold models/lang need runData. Default on.