+++
title = "GTMQoLMultiblocks: registration and recipes of SAF, DTFR, ILCR, void miner, fishing pond, behind machines.* toggles"
keys = [ "addcyclicmachinelang", "machines config", "magical assembler recipe", "multiblock base" ]
updated = "2026-10-21 08:14:03"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "public static void init() {\n        GTMQoLConfig.Machines config = GTMQoLConfig.get().machines;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (config.machines.electricImplosionCompressor) ElectricImplosion.init(modBus);"
+++
One init/recipe method per machine in GTMQoLMultiblocks (SAF, DTFR, ILCR, void miner, fishing pond); the electric implosion compressor, crystal chamber and greenhouse have their own classes, initialised from GTMQoL and given recipes in GTMQoLAddon. Lang and models are datagen'd. All five GTMQoLMultiblocks controllers are crafted in the magical assembler. Gregified, modular and steam multiblocks live elsewhere.