+++
title = "ME Assembler / ME Circuit Slicer, silicon chips and v7 AE recipes (AEProcessing, config ae2.processing)"
keys = [ "advanced_ae", "appflux", "extendedae_plus", "me_assembler", "me_circuit_slicer", "megacells", "print", "processor" ]
updated = "2026-10-21 08:27:56"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "public final class AEProcessing {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "        ME_ASSEMBLER_RECIPES = GTRecipeTypes.register(GTMQoL.id(\"me_assembler\"), GTRecipeTypes.ELECTRIC)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "    public static void addRecipes(Consumer<FinishedRecipe> provider) {"

[[items]]
name = "machine-names"
value = "Machine names come from SimpleMachineBuilder's toEnglishName, so they read \"Basic Me Assembler\" (lowercase e); the recipe type lang is \"ME Assembler\". Cosmetic, not fixed."

[[items]]
name = "1.20.1-left-out"
value = "Not ported from 1.21: ExtendedAE entro / concurrent processor recipes (expatternprovider 1.4.18 has neither) and EAP lattra recipes (EAP 1.6.1 has lattra_crystal / oblivion_singularity, no lattra_dust). Optional recipes check isModLoaded and != AIR, so a wrong id skips silently."

[[items]]
name = "forge-silicone-guess"
value = "1.21's c: tags became forge:; forge:silicone for the insulating resin mixer recipe is a guess, unchecked."
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEProcessing.java"
snippet = "ResourceLocation.fromNamespaceAndPath(\"forge\", \"silicone\")"
+++
Port of v7 QoLRecipeTypes/QoLMachines/QoLRecipes. Items in the mod constructor, recipe types and machines from GTCEu's RegisterEvents. Machine crafting with an AE2 inscriber; slicer: wafer -> chips, AE2 materials -> prints; assembler: chip + print + (silicon print or 4 copper foil) + redstone -> processors, GTCEu ME parts from AE2 parts; misc in GTCEu wiremill/polarizer/mixer/macerator/implosion/compressor. Optional advanced_ae/megacells/appflux/extendedae_plus recipes: ids unchecked on 1.20.1.