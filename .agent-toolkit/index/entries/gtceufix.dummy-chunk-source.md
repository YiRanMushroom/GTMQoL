+++
title = "DummyChunkSourceMixin: lock ModularUI preview's dummy chunk map against concurrent mesh builds (crash)"
keys = [ "arrayindexoutofboundsexception", "batching chunks", "computeifabsent", "modularui bug", "multiblock preview" ]
updated = "2026-10-29 05:08:00"
checked = "2026-10-29"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/DummyChunkSourceMixin.java"
snippet = "public class DummyChunkSourceMixin {"
+++
Both branches, client config. The crash was first seen in ATM10 (1.21.1). getChunk is a vanilla (remapped) method. 1.21.1 also wraps MUI's clear (remap = false); 1.20.1's MUI (builds 23-25) has no clear, so the 1.20.1 mixin only wraps getChunk.