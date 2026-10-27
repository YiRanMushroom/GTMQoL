+++
title = "BuildSettings: layers, sizes, block per char and per position, from the client's preview; sent with each click"
keys = [ "auto-build", "autobuildpanel.settings", "blockchoice" ]
updated = "2026-10-27 15:31:30"
checked = "2026-10-27"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/BuildSettings.java"
snippet = "public record BuildSettings(Int2IntMap sliceRepeats, IntList dimensions, Char2ObjectMap<BlockState> blocks,"
+++
Made from the client's MultiblockSchemaInfo (autobuild.panel), kept in memory only. Block states go by Block.getId, as in GTCEu's terminal packet. The planner checks every choice against the pattern (autobuild.planner).