+++
title = "VoidMinerOres: ore chances from GTCEu ORE_VEIN registry for the dimension, weighted by vein weight; dimensionMapping"
keys = [ "gtnh veins", "ore prefix", "void miner", "voidminer.dimensionmapping" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/VoidMinerOres.java"
snippet = "public static List<OreChance> compute(ServerLevel level) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/VoidMinerOres.java"
snippet = "private static TagPrefix orePrefix(MinecraftServer server, ResourceKey<Level> dimension) {"
+++
The user's "GTNH veins" means GT's own veins. voidMiner.dimensionMapping ("from=to") mines another dimension's veins and stone. Computed once per machine load; a datapack /reload needs a chunk reload.