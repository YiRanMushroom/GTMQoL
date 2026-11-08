+++
title = "Pattern Batching on ME Pattern Buffers: mixin, unwrap and the batch popup (own logic, no EAP since 2026-11)"
keys = [ "batch", "pushpattern" ]
updated = "2026-11-08 05:04:17"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferPatternBatchingMixin.java"
snippet = "public abstract class MEPatternBufferPatternBatchingMixin"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/patternbuffer/PatternBatching.java"
snippet = "public final class PatternBatching"
+++
Worked out in ae2.fast-crafting; this is the buffer's side. The buffer is a PatternBatchProvider: processing patterns, limit from the popup (0 = none, else most of any input per batch, in items or buckets). pushPattern unwraps PatternBatch because slots match by equals (user decision, enlarged buffers too); @ModifyArg (require = 2) on both InternalSlot.pushPattern calls pushes the batch's inputs. Saved fields kept their gtmqol$ names, so settings survived leaving EAP. UI: "×2" popup.