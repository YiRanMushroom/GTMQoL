+++
title = "CyclicMultiblockMachine: fixed-length cycles without recipe logic, keeps overflow; base of void miner and fishing pond"
keys = [ "finishcycle", "multiblock base", "retry_ticks", "startcycle", "status line", "stored popup" ]
updated = "2026-10-19 20:27:55"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/CyclicMultiblockMachine.java"
snippet = "public abstract class CyclicMultiblockMachine extends MultiblockControllerMachine implements IMuiMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/CyclicMultiblockMachine.java"
snippet = "private void tick() {"
+++
An abstraction the user asked for. Own server tick because GTCEu's recipe logic voids overflowing outputs. RECIPE_LOGIC_STATUS is set by hand (WORKING only while working). Common lang keys come from GTMQoLMultiblocks.addCyclicMachineLang. Popups and counter rows: rules.ui-popups. 1.20.1: the stored list syncs through GenericListSyncHandler.Builder<T> with Stored::read/write (FriendlyByteBuf).