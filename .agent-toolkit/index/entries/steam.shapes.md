+++
title = "AdvancedSteamShapes: large steam multiblock structures converted once from GTNL's .mbs files"
keys = [ "large steam multis", "mbs1", "pattern", "sciencenotleisure", "steam_manufacturer" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamShapes.java"
snippet = "final class AdvancedSteamShapes {"
+++
Generated once by a Python script (not in the repo) from GTNL's assets/sciencenotleisure/multiblock/large_steam_*.mbs. Format: "MBS1", a string table (int length + UTF-8), then rows of int indices; layout shape[row top->bottom][slice front->back], strings along X, ~ controller, space any block. Rows reversed to bottom-up for MultiblockPatternBuilder.start(). GTNL letters remapped to one bronze symbol set (class javadoc). Possibly mirrored vs GTNL; harmless.