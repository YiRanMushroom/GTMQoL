+++
title = "Steam Parallel Control Hatch: 16-256 parallels for advanced steam multis, own PartAbility, not shareable"
keys = [ "large steam multis", "steam_parallel", "steam_parallel_hatch" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamMachines.java"
snippet = "public static final PartAbility STEAM_PARALLEL = new PartAbility(\"steam_parallel_hatch\");"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/SteamParallelHatchPartMachine.java"
snippet = "public class SteamParallelHatchPartMachine extends MultiblockPartMachine implements IMuiMachine {"
+++
Default 256. Recipe (magical assembler) uses 4 LV circuits on purpose: the user wants no LV machines or circuits in the large steam multis themselves, only in this optional hatch.