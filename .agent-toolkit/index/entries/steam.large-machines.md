+++
title = "AdvancedSteamMachines: gtmqol:large_steam_* for 21 recipe types, hatch casing rules, magical assembler recipes"
keys = [ "autoabilities", "bronze", "hp_steam_magical_assembler", "large steam multis", "lp_steam_magical_assembler", "steam bus", "steam multiblock" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamMachines.java"
snippet = "private static void register(String name, Supplier<GTRecipeType> recipeType, int maxRecipeTier, ResourceLocation overlay,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamMachines.java"
snippet = "public static void addRecipes(RecipeOutput provider) {"
+++
User choices: all bronze (GTCEu steam multis have no steel tier); circuit assembler is MV so it can make the ULV-HV control circuits; magical assembler LV. Multis cost no circuits and ULV EU/t so the steam magical assembler can make them. HP steam magical assembler keeps GTCEu's steel theme like other HP single blocks. Shapes: steam.shapes; new steam single blocks: steam.single-blocks-new.