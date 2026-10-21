+++
title = "AutoBuilder: places planned blocks from the player's inventory, 64 per batch, jobs every 5 ticks"
keys = [ "auto-build", "blocks_per_batch", "checkandformstructure", "entityplaceevent", "missing items", "onblockplace", "structure cache" ]
updated = "2026-10-19 20:12:36"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuilder.java"
snippet = "private static Batch placeBatch(ServerPlayer player, ServerLevel level, List<StructurePlanner.Entry> plan) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuilder.java"
snippet = "private static boolean afterBatch(ServerPlayer player, MultiblockControllerMachine controller, Batch batch) {"
+++
Jobs are keyed by dimension + controller pos and stop when done, formed, stuck, the player is away (>64 blocks)/offline, or the controller is gone. Structure is only checked after the last batch (cache handling explained in the code comment). Forge 47: TickEvent.ServerTickEvent END phase, ForgeEventFactory.onBlockPlace, rollback via snapshot.restore(true, false). 1.21.1 has the same on NeoForge events.