+++
title = "Steam Parallel Control Hatch: 16-256 parallels for advanced steam multis, own PartAbility, not shareable"
keys = [ "large steam multis", "steam_parallel", "steam_parallel_hatch" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/SteamParallelHatchPartMachine.java"
snippet = "public class SteamParallelHatchPartMachine extends MultiblockPartMachine implements IMuiMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamMachines.java"
snippet = "public static final PartAbility STEAM_PARALLEL = new PartAbility(\"steam_parallel_hatch\");"
+++
Default 256. Recipe (magical assembler) uses 4 LV circuits on purpose: the user wants no LV machines or circuits in the large steam multis themselves, only in this optional hatch.