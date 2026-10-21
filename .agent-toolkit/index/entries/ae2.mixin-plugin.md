+++
title = "AE2MixinPlugin: which gtmqol.ae2.mixins.json mixins apply (AE2/EMI/ExtendedAE present, early config switches)"
keys = [ "ae2.stickycard", "ae2.universalcircuitencoding", "gtmqol-early.properties" ]
updated = "2026-10-19 20:08:33"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/AE2MixinPlugin.java"
snippet = "if (mixinClassName.endsWith(\"SharedHatchesMixin\")) return true;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/AE2MixinPlugin.java"
snippet = "public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {"
+++
Matched by name: encoding (early switch), EMI encoding, MEStocking* and MEPatternBuffer* (always), *SharedHatchesMixin (always; shared hatches are toggled per buffer at runtime). Anything else is the sticky card (ae2.stickyCard). Before 2026-10-20 the shared-hatches mixins that don't start with MEPatternBuffer fell into the sticky-card bucket. ExtendedAE mod id is "extendedae" (v7's 1.20 fork used "expatternprovider"). EAP mixins use the separate EAPMixinPlugin (ae2.smart-doubling).