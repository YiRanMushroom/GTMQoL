+++
title = "Modern Industrialization source: 14 single blocks + 10 multiblocks, energy-preserving converter, coil temps"
keys = [ "blast furnace", "guidebook", "machinerecipe", "mi_", "mimachinerecipetypes", "modern industrialization", "widentogt" ]
updated = "2026-11-12 07:35:59"
checked = "2026-11-12"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mi/MIGregification.java"
snippet = "public static List<ForeignMachineType> types() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mi/MIGregification.java"
snippet = "private static boolean convert(RecipeHolder<?> holder, GTRecipeBuilder builder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mi/MIGregification.java"
snippet = "private static Ingredient widenToGT(Ingredient ingredient) {"

[[links]]
path = "gradle.properties"
snippet = "mi_version=HOR1tVas"
+++
1.21 only: MI on 1.20.1 is Fabric-only (NeoForge from 1.20.4, no Forge port); Sinytra Connector would need rewriting the converter for Fabric fluid API. Config gregification.modernIndustrialization. MI fluid->GT material is a name guess only. Blast furnace UI shows GT's coil line (TEMP_COIL_INFO). HOR1tVas = MI 2.5.10.

MI's recipes aren't in its jar (generated at runtime), so slot counts come from the dev log (2026-11-12): GT warns once per output past the type's maximum. Multiblocks have 6 slots per kind, the quarry 10 item outputs (gold: 10, bronze/steel: 9) and the distillation tower 8 fluid outputs (steam_cracked_naphtha_full). MARKED single blocks (assembler, distillery, mixer) go through MarkCoveredRecipes: the distillery has one recipe per product of the same fluid, the mixer's he/le MOX and uranium recipes differ only in amounts, and a recipe whose inputs are a prefix of another's (purple dye) can't enter GT's lookup at all ("without exact duplicate/conflict"); without marks GT dropped 23 of them.