+++
title = "DummyChunkSourceMixin: lock ModularUI preview's dummy chunk map against concurrent mesh builds (crash)"
keys = [ "arrayindexoutofboundsexception", "atm10", "batching chunks", "computeifabsent", "modularui bug", "multiblock preview" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/DummyChunkSourceMixin.java"
snippet = "public class DummyChunkSourceMixin {"
+++
Both branches, client. Crash was seen in ATM10.