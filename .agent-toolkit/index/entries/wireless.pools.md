+++
title = "Wireless pools and IO stats: WirelessPool SavedData (BigInteger per network) and monitor rates"
keys = [ "moveall", "wirelessenergysaveddata", "wirelesssteamsaveddata" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/IOStats.java"
snippet = "public final class IOStats {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessMonitorMachine.java"
snippet = "public abstract class WirelessMonitorMachine extends MetaMachine implements IMuiMachine, IDataStickInteractable {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessNetworks.java"
snippet = "    static List<WirelessPool> pools() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessPool.java"
snippet = "public abstract class WirelessPool extends SavedData {"
+++
Stats are deliberately not saved (user agreed); sampled on ServerTickEvent END. A new pool kind must be added to WirelessNetworks.pools() (stat sampling, FTB team moves) and to WirelessBindingTrait.movePrivateToTeam, which calls each pool itself because its message names both amounts. WirelessEnergySavedData also stores the per-player default amperage (wireless.energy-amperage).