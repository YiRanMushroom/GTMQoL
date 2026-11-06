+++
title = "FE from the wireless energy network: input/output hatches, energy cube, AE2 wireless energy cell (config wireless.fe)"
keys = [ "energy cube", "fe input hatch", "fe output hatch", "iaepowerstorage", "ienergystorage", "part ability", "wireless energy cell" ]
updated = "2026-11-06 18:22:22"
checked = "2026-11-06"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessFEBuffer.java"
snippet = "public final class WirelessFEBuffer"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessFEMachines.java"
snippet = "public static final PartAbility FE_INPUT"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessEnergyCubeMachine.java"
snippet = "public class WirelessEnergyCubeMachine"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/WirelessEnergyCellMachine.java"
snippet = "private void recheck()"
+++
User's design: wireless EU as FE, no storage, no rate. Input hatch gives the controller FE, output hatch takes it (extractFE/insertFE, for later multiblocks; no multiblock uses the abilities yet). Cell: AE2 drops a storage that falls short, so it re-adds itself every second; its max is constant because AE2 sums it when it joins. Cells on one wireless network in a grid: only one reports the stored amount, so AE2 counts it once; the max still adds up. Cube sides: EnergyCubeSidesRender.