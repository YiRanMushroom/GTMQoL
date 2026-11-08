+++
title = "AE2MixinPlugin: which gtmqol.ae2.mixins.json mixins apply (AE2/EMI/ExtendedAE present, early config switches)"
keys = [ "ae2.stickycard", "ae2.universalcircuitencoding", "gtmqol-early.properties" ]
updated = "2026-11-08 03:56:31"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/AE2MixinPlugin.java"
snippet = "if (mixinClassName.endsWith(\"SharedHatchesMixin\")) return true;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/AE2MixinPlugin.java"
snippet = "public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {"
+++
Matched by name: encoding (early switch), EMI encoding (EMI loaded), MEStocking*, MEPatternBuffer*, Crafting* (pattern batching and crafting plans, ae2.fast-crafting), PatternEncodingTermMenuUploadMixin (upload to the Assembling Matrix) and *SharedHatchesMixin (shared hatches are toggled per buffer at runtime): all always. Anything else is the sticky card (ae2.stickyCard); its ExtendedAE mixin needs mod id "extendedae" (v7's 1.20 fork used "expatternprovider"). There is no EAP plugin any more (pattern batching is our own since 2026-11).