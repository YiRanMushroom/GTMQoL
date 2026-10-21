+++
title = "Modern Industrialization source: 14 single blocks + 10 multiblocks, energy-preserving converter, coil temps"
keys = [ "blast furnace", "guidebook", "machinerecipe", "mi_", "mimachinerecipetypes", "modern industrialization", "widentogt" ]
updated = "2026-10-19 20:08:34"

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