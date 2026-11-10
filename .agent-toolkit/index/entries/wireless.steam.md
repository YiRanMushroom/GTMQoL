+++
title = "Wireless steam: input/output hatches, accessor and monitor over a pool-backed tank (WirelessSteamTank)"
keys = [ "overlay_wireless", "steam multiblock", "texture", "wireless_steam_accessor", "wireless_steam_input_hatch", "wireless_steam_monitor", "wireless_steam_output_hatch" ]
updated = "2026-11-10 07:38:19"
checked = "2026-11-10"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessUI.java"
snippet = "public static Flow create(PanelSyncManager syncManager, WirelessBindingTrait binding, Player player,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/steam/WirelessSteamMachines.java"
snippet = "    private static final ResourceLocation WIRELESS_OVERLAY = GTMQoL.id(\"block/overlay/machine/overlay_wireless\");"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/steam/WirelessSteamTank.java"
snippet = "public class WirelessSteamTank extends CustomFluidTank {"
+++
Config wireless.steam. The input hatch has PartAbility.STEAM and IMPORT_FLUIDS (user's request 2026-11-10): steam multiblocks take it as their steam hatch, other multiblocks as a fluid input hatch for recipes using steam; the output hatch has EXPORT_FLUIDS. overlay_wireless is the animated rainbow swirl from GTMThings' overlay_energy_on_wireless (originally GTNH); credits in README.md. Hatches use it with no pipe/emissive layer so input and output look identical; it is shared by the EU hatches and all covers. WirelessUI is the shared bind/unbind/private/To-team UI; WirelessUI.addSharedLang is called by every kind and adds the lang once. The tank accepts fluids in the steam fluid tag.