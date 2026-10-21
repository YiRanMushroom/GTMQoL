+++
title = "ME Assembler / ME Circuit Slicer, silicon chips and v7 AE recipes (AEProcessing, config ae2.processing)"
keys = [ "advanced_ae", "appflux", "extendedae", "me_assembler", "me_circuit_slicer", "megacells", "print", "processor" ]
updated = "2026-10-19 20:08:33"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "public final class AEProcessing {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "        ME_ASSEMBLER = new GTMachineUtils.SimpleMachineBuilder(GTMQoLAddon.registrate(), \"me_assembler\","

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "    public static void addRecipes(RecipeOutput provider) {"
+++
Port of v7 QoLRecipeTypes/QoLMachines/QoLRecipes. Recipes: machine crafting with an AE2 inscriber in the middle; wafer -> chips (8/16/32/64); AE2 materials -> prints; chip + print + silicon print (or 4 copper foil) + 144 mB redstone -> processors x chip multiplier; GTCEu ME parts from AE2 parts; wiremill/polarizer/mixer recipes. Integration-mod recipes (extendedae, advanced_ae, megacells, appflux) are 1.21 only. Block names read "Me Assembler": SimpleMachineBuilder's toEnglishName.