+++
title = "Smart doubling on pattern buffers: mixin, unwrap and the x2 popup (own logic, no EAP since 2026-11)"
keys = [ "batch", "pushpattern" ]
updated = "2026-11-07 07:19:08"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferSmartDoublingMixin.java"
snippet = "public abstract class MEPatternBufferSmartDoublingMixin"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/SmartDoubling.java"
snippet = "public final class SmartDoubling"
+++
Worked out in ae2.fast-crafting; this is the buffer's side. The buffer is a BatchCraftingProvider: processing patterns, limit from the popup (0 = none, else most of any input per batch, in items or buckets). pushPattern unwraps ScaledPattern because slots match by equals (user decision, enlarged buffers too); @ModifyArg (require = 2) on both InternalSlot.pushPattern calls pushes the batch's inputs. Saved fields kept their gtmqol$ names, so settings survived leaving EAP. UI: "×2" popup.