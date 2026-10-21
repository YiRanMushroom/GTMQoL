+++
title = "BuildSettings: slice repeats, expandable bounds, block choices; client-side per multiblock type, sent with each click"
keys = [ "auto-build", "autobuildpanel.settings", "blockchoice" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/BuildSettings.java"
snippet = "public record BuildSettings(Int2IntMap sliceRepeats, IntList dimensions, Char2ObjectMap<BlockChoice> blocks) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuildPanel.java"
snippet = "private static final Map<ResourceLocation, BuildSettings> SETTINGS = new HashMap<>();"
+++
