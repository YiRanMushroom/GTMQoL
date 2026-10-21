+++
title = "EU <-> FE: every GT machine/cable EU input also accepts FE (FEInputProvider, config misc.feInput)"
keys = [ "energy storage", "eutofeprovider", "fetoeuratio", "forge energy", "nativeeutofe", "registercapabilitiesevent" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/fe/FEInputProvider.java"
snippet = "public final class FEInputProvider {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/fe/FEInputProvider.java"
snippet = "    private static @Nullable IEnergyContainer energyContainer(@Nullable BlockEntity blockEntity,"
+++
User wanted every EU input to take FE and every output to give FE; outputs are covered by GTCEu's own EUToFEProvider. Packets are capped at the input voltage so nothing explodes. Pull-based FE pipes still can't extract from GT machines. 1.20.1 uses AttachCapabilitiesEvent instead.