+++
title = "MEPatternBufferInternalSlotMixin: pattern buffer fluid keys never merged (FluidStack without equals)"
keys = [ "fluidinventory", "fluidstack equals" ]
updated = "2026-10-27 15:11:02"
checked = "2026-10-27"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/MEPatternBufferInternalSlotMixin.java"
snippet = "public class MEPatternBufferInternalSlotMixin {"
+++
1.21 only. The first half (slots failed any ingredient >1 because of SizedIngredient.test's count check) was fixed upstream in #5568 (build 130, 2026-10-25) and removed here. Still needed: the fluid map keyed by FluidStack without equals/hashCode (documented in the class).