+++
title = "GTMQoLMultiblocks: registration and recipes of SAF, DTFR, void miner, fishing pond, each behind a machines.* toggle"
keys = [ "addcyclicmachinelang", "machines config", "magical assembler recipe", "multiblock base" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "public static void init() {\n        GTMQoLConfig.Machines config = GTMQoLConfig.get().machines;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (config.machines.electricImplosionCompressor) ElectricImplosion.init(modBus);"
+++
One init/recipe method per machine in GTMQoLMultiblocks; the electric implosion compressor, crystal chamber and greenhouse have their own classes, initialised from GTMQoL and given recipes in GTMQoLAddon. Lang and models are datagen'd. The SAF, DTFR, void miner and fishing pond controllers are crafted in the magical assembler. Gregified, modular and steam multiblocks live elsewhere.