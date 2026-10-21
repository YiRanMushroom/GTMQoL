+++
title = "EU <-> FE: every GT machine/cable EU input also accepts FE (FEInputProvider, config misc.feInput)"
keys = [ "attachcapabilitiesevent", "energy storage", "eutofeprovider", "fetoeuratio", "forge energy", "nativeeutofe" ]
updated = "2026-10-19 20:12:38"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/fe/FEInputProvider.java"
snippet = "public final class FEInputProvider implements ICapabilityProvider {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/fe/FEInputProvider.java"
snippet = "    private @Nullable IEnergyContainer energyContainer(@Nullable Direction side) {"
+++
User wanted every EU input to take FE and every output to give FE; outputs are covered by GTCEu's own EUToFEProvider. Packets are capped at the input voltage so nothing explodes. Pull-based FE pipes still can't extract from GT machines. Attached via AttachCapabilitiesEvent<BlockEntity> on MetaMachine / CableBlockEntity; MetaMachine.getCapability asks its own capabilities before attached ones, so a machine with its own FE storage keeps it. 1.21 uses RegisterCapabilitiesEvent.