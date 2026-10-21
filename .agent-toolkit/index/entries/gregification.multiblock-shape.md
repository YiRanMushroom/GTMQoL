+++
title = "MultiblockShape: structures of gregified multiblocks (GENERIC, ANY_PARTS, EBF, vacuum freezer, Create casings...)"
keys = [ "casingparts", "create_assembly_line", "create_brass", "distillation_tower", "electric_blast_furnace", "gregification core", "implosion_compressor", "pattern", "vacuum_freezer" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/MultiblockShape.java"
snippet = "public enum MultiblockShape {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "private static void declareMultiblock(ForeignMachineType type, GTRecipeTypeEntry recipeType) {"
+++
New GT-like shape: copy the pattern from gtceu GTMultiMachines and swap in controller(definition). ANY_PARTS exists for capabilities that arrive through parts of other abilities (Mek chemicals via universal ME parts / pattern buffer). Default multiblock modifiers: COIL_TEMPERATURE, OVERCLOCK, gtceu's BATCH_MODE (see gregification.modifiers); withModifiers overrides.