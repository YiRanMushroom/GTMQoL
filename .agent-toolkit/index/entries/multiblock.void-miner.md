+++
title = "Void Miner: wireless-EU-paid ore generation, 1-16 operations x 1-16 stacks per 15 s cycle, no energy hatch"
keys = [ "eu_per_stack", "multiplier", "void miner", "wirelessbindingtrait" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/VoidMinerMachine.java"
snippet = "public class VoidMinerMachine extends CyclicMultiblockMachine implements IDataStickInteractable {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/VoidMinerMachine.java"
snippet = "protected @Nullable String startCycle() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "private static void initVoidMiner() {"
+++
Semantics as corrected by the user: one stack is 64 of the *same* ore; each operation draws one ore and yields `multiplier` stacks of it, so operations = max distinct ores per cycle. Paid all-or-nothing up front from the wireless network (1M EU/stack); settings are snapshotted at payment. Shape: GTCEu's EV Large Miner in solid steel, DUMMY_RECIPES, 'X' takes only output buses. Ores: multiblock.void-miner-ores. Recipe: LV, no chips (skyblock).