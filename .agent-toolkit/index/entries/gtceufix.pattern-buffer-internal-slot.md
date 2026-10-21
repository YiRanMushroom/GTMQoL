+++
title = "MEPatternBufferInternalSlotMixin: pattern buffer slots failed any ingredient >1 (count test); fluid keys never merged"
keys = [ "fluidinventory", "fluidstack equals", "handlefluidinternal", "handleiteminternal", "insufficient item", "parallel", "sizedingredient" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/MEPatternBufferInternalSlotMixin.java"
snippet = "public class MEPatternBufferInternalSlotMixin {"
+++
1.21 only. Symptom: with parallel even a one-in/one-out recipe showed "insufficient item", while the parallel count (getMaxByInput) was right. The second half (fluid map keyed by FluidStack without equals/hashCode) is documented in the class.