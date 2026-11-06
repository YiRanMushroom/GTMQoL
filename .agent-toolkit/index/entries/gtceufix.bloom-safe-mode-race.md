+++
title = "Known, not fixed: GTCEu bloom safe mode crashes with \"Not building!\" when two rebuilds of a section overlap"
kind = "status"
keys = [ "bloom", "bufferbuilder", "crash", "not building", "pitfall", "safe_mode" ]
updated = "2026-11-06 08:20:19"
checked = "2026-11-06"
+++
Upstream GTCEu bug, seen twice on 2026-11-06 on 1.21 (build 133) with `client.bloom.safe_mode=true`, without Sodium: `IllegalStateException: Not building!` in `BloomRenderer.copyBloomQuad` during chunk meshing, thrown on whichever glowing block was being written (an HPCA heat sink, then an HPCA controller). Our code isn't in the stack. The user chose not to fix it (2026-11-06).

Cause, read from the code and not reproduced: SafeMode keeps one BufferBuilder per section. Each glowing quad gets it from `getOrStartBloomBuffer` and writes outside any lock. Two rebuilds of one section can overlap, since a cancelled task keeps running. The first to end bakes the builder (`bakeBloomChunkBuffers`) while the other is still writing to it. `BloomSafeModeMixin` is unrelated: it only drops stale buffers.

Workarounds: restart, it is timing dependent; or `client.bloom.safe_mode=false` in `config/gtceu-early.properties` (gtceu's default).

If it ever needs fixing: a `gtceufix` mixin making "get the builder and write the quad" and the bake mutually exclusive, e.g. `synchronized (BloomRenderer.SafeMode.class)` around the `Consumer.accept` in `copyBloomQuad` (fetching the builder again inside) and around `bakeBloomChunkBuffers`. Only quads with bloom take the lock.