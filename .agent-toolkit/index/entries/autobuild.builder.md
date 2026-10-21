+++
title = "AutoBuilder: places planned blocks from the player's inventory, 64 per batch, jobs every 5 ticks"
keys = [ "auto-build", "blocks_per_batch", "checkandformstructure", "entityplaceevent", "missing items", "structure cache" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuilder.java"
snippet = "private static Batch placeBatch(ServerPlayer player, ServerLevel level, List<StructurePlanner.Entry> plan) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuilder.java"
snippet = "private static boolean afterBatch(ServerPlayer player, MultiblockControllerMachine controller, Batch batch) {"
+++
Jobs are keyed by dimension + controller pos and stop when done, formed, stuck, the player is away/offline, or the controller is gone. Structure is only checked after the last batch (the cache handling there is explained in the code comment).