+++
title = "ME Assembler / ME Circuit Slicer, silicon chips and v7 AE recipes (AEProcessing, config ae2.processing)"
keys = [ "advanced_ae", "appflux", "extendedae", "me_assembler", "me_circuit_slicer", "megacells", "print", "processor" ]
updated = "2026-11-11 07:39:07"
checked = "2026-11-11"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "public final class AEProcessing {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "private static MachineEntry<MachineDefinition>[] machines(String name, String englishName,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "    public static void addRecipes(RecipeOutput provider) {"
+++
Port of v7 QoLRecipeTypes/QoLMachines/QoLRecipes. Machines crafted with an AE2 inscriber in the middle; wafer -> chips (8/16/32/64); AE2 materials -> prints; chip + print + silicon print (or 4 copper foil) + 144 mB redstone -> processors x chip multiplier; GTCEu ME parts from AE2 parts; wiremill/polarizer/mixer/macerator recipes. Integration recipes (extendedae(_plus), advanced_ae, megacells, appflux; incl. implosion/compressor): 1.21 only. The machines are registered through GTMQoLAddon.tieredMachine with their own name ("ME Assembler"): gtceu's SimpleMachineBuilder makes the name from the id ("Me Assembler") and has no hook; same ids, model and machine class family, so placed ones carry over (untested).