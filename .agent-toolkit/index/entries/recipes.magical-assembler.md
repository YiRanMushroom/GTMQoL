+++
title = "Magical assembler: always-on recipe type and machines for GTMQoL's own recipes"
keys = [ "circuit conversion", "creative data access hatch", "gtmqol:magical_assembler", "rubber sapling", "simplemachinebuilder" ]
updated = "2026-10-19 20:10:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "RECIPE_TYPE = GTMQoLAddon.registrate().recipeType(\"magical_assembler\", GTRecipeTypes.ELECTRIC)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "public static void addRecipes(RecipeOutput provider) {"
+++
Always on because most of our recipes use it. Textures copied from the 7.x mod. Ported from the old `QoLRecipes.kt`; the probable (im)probability devices and the industrial LCR were not ported because their outputs no longer exist.