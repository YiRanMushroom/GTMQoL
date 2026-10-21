+++
title = "Legacy part recipes: rod/bolt/gear/small gear/rotor (+ long rod) extruder molds that GTCEu v8 removed"
keys = [ "materialamount", "onregistertagprefixes", "recipes.uselegacypartrecipes", "uselegacypartrecipes" ]
updated = "2026-10-21 08:30:51"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "if (config.recipes.useLegacyPartRecipes) LegacyPartRecipes.addRecipes(provider);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "modBus.addGenericListener(TagPrefix.class, LegacyPartRecipes::onRegisterTagPrefixes);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/part/LegacyPartRecipes.java"
snippet = "rotor.materialAmount(M * 4);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/part/LegacyPartRecipes.java"
snippet = "private static void addMoldRecipes(Consumer<FinishedRecipe> provider) {"
+++
GTCEu v8 (both branches) makes rods on the lathe, gears in the forming press, rotors in the welder (4 plates + ring). Extruder recipes copied from v7/early v8 MaterialRecipeHandler/PartsRecipeHandler. Rotor back to M*4 (v8: M*17/4) applies to welded rotors too; set in the TagPrefix RegisterEvent since touching TagPrefix during mod construction registers prefixes too late. Ids gtmqol:extrude_<material>_... avoid clashes. Mold models/lang need runData. Default on.