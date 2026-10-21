+++
title = "MultiblockShape: structures of gregified multiblocks (GENERIC, ANY_PARTS, EBF, vacuum freezer, Create casings...)"
keys = [ "casingparts", "create_assembly_line", "create_brass", "distillation_tower", "electric_blast_furnace", "gregification core", "implosion_compressor", "pattern", "vacuum_freezer" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "private static void declareMultiblock(ForeignMachineType type, GregifiedRecipeType recipeType) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/MultiblockShape.java"
snippet = "public enum MultiblockShape {"
+++
New GT-like shape: copy the pattern from gtceu GTMultiMachines and swap in controller(definition). ANY_PARTS exists for capabilities arriving through parts of other abilities (Mek chemicals via universal ME parts / pattern buffer). Default modifiers: COIL_TEMPERATURE, OVERCLOCK, gtceu's BATCH_MODE (gregification.modifiers); withModifiers overrides. Used on 1.20.1: ANY_PARTS (Mek) and the three CREATE shapes; the GT-like ones (EBF, freezer...) serve MI on 1.21.