+++
title = "Wireless EU hatches and accessor: per-tick V x A budget container, overclock mode, re-form on settings change"
keys = [ "calculatevoltageamperage", "dynamo", "energycontainerlist", "getcontents", "gettotalcontentamount", "substation", "wireless_energy_input_hatch" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessEnergyContainer.java"
snippet = "public class WirelessEnergyContainer extends NotifiableEnergyContainer {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessEnergyContainer.java"
snippet = "    public List<EnergyStack> handleRecipeInner(IO io, GTRecipe recipe, List<EnergyStack> left, boolean simulate) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessEnergyHatchPartMachine.java"
snippet = "    public void applySettings(int amperage, boolean overclocked) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessEnergyAccessorContainer.java"
snippet = "public class WirelessEnergyAccessorContainer extends NotifiableEnergyContainer {"
+++
One per tier in GTMachineUtils.ALL_TIERS. Re-forming every controller on apply (Save button or UI close) was approved by the user. Accessor sides accept any voltage (user's choice). The prime-factor slowness (~77% of a server profile at 2^24 A) is also fixed upstream-side by gtceufix/EnergyContainerListMixin. Known limit: GTCEu sums hatches in longs; ~256 full-amperage MAX hatches on one multiblock overflow. Monitor: single LV machine.