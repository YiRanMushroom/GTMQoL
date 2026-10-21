+++
title = "Wireless EU amperage: 1..2^24 input row (x/÷ 4, Shift 16, Ctrl 2) and per-player default (/gtmqol default_amperage)"
keys = [ "amperagerow", "max_amperage" ]
updated = "2026-10-19 20:08:35"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessEnergyHatchPartMachine.java"
snippet = "    public static final int MAX_AMPERAGE = 1 << 24;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessEnergyUI.java"
snippet = "    public static Flow amperageRow(PanelSyncManager syncManager, IntSupplier getter, IntConsumer setter) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/DefaultAmperageCommand.java"
snippet = "public final class DefaultAmperageCommand {"
+++
2^24 max is the user's choice. The default is stored in WirelessEnergySavedData and applied in WirelessNetworks.onEntityPlace to hatches and accessors the player places (others get 4); covers use it too.