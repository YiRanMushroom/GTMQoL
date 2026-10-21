+++
title = "DummyChunkSourceMixin: lock ModularUI preview's dummy chunk map against concurrent mesh builds (crash)"
keys = [ "arrayindexoutofboundsexception", "batching chunks", "computeifabsent", "modularui bug", "multiblock preview" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/DummyChunkSourceMixin.java"
snippet = "public class DummyChunkSourceMixin {"
+++
Both branches (identical code), client config. The crash was first seen in ATM10 (1.21.1). getChunk is a vanilla (remapped) method, clear is MUI's (remap = false).