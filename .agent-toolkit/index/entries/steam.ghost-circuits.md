+++
title = "Ghost circuits on steam machines: steam panel trait configurators back on, steam input bus circuit slot enabled"
keys = [ "addtraitconfigurators", "defaultsteammachinepanelbuilder", "steam single blocks", "steamtweaks.circuitslots" ]
updated = "2026-10-19 20:08:35"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTSingleblockMachinePanelsMixin.java"
snippet = "public class GTSingleblockMachinePanelsMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/SteamItemBusPartMachineMixin.java"
snippet = "public class SteamItemBusPartMachineMixin {"
+++
The slot itself is attached by SimpleSteamMachineMixin (steam.single-block-io). The bus slot gives steam multis ghost circuits (input bus only).