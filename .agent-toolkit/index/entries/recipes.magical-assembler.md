+++
title = "Magical assembler: always-on recipe type and machines for GTMQoL's own recipes"
keys = [ "circuit conversion", "creative data access hatch", "gtmqol:magical_assembler", "rubber sapling", "simplemachinebuilder" ]
updated = "2026-10-21 08:13:50"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "RECIPE_TYPE = GTMQoLAddon.registrate().recipeType(\"magical_assembler\", GTRecipeTypes.ELECTRIC)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "public static void addRecipes(RecipeOutput provider) {"
+++
Always on because most of our recipes use it (universal circuits, wireless parts, creative data access hatch, rubber sapling, SAF/DTFR/ILCR/void miner/fishing pond controllers, probable devices, ghost hatch, modular conversions). Textures copied from the 7.x mod. Ported from the old `QoLRecipes.kt`. Default recipe EU/t is VA[LV] (prepareBuilder).