+++
title = "Wireless energy (per tier) and steam covers: bound to the attaching player, move V x A EU/t or steam each tick"
keys = [ "boiler", "changeenergy", "onregistercovers", "wireless_energy_input", "wirelesssteamcover" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessCover.java"
snippet = "public abstract class WirelessCover extends CoverBehavior implements IMuiCover {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessCovers.java"
snippet = "public final class WirelessCovers {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessEnergyCover.java"
snippet = "public class WirelessEnergyCover extends WirelessCover {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "    private void onRegisterCovers(GTCEuAPI.RegisterEvent<ResourceLocation, CoverDefinition> event) {"
+++
Covers have no machine, so they hold a WirelessBindingTrait built with the (ISyncManaged, BooleanSupplier) constructor (see wireless.networks). No data stick support on covers. Registered with GTCovers.register from GTCEu's cover RegisterEvent (configs wireless.energy / wireless.steam); 1.21 uses registrate().cover in the mod constructor. Energy covers take the player's default amperage when attached.