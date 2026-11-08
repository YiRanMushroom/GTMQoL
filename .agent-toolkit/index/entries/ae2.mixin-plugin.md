+++
title = "AE2MixinPlugin: which gtmqol.ae2.mixins.json mixins apply (AE2/EMI/JEI/ExtendedAE present, early config switches)"
keys = [ "ae2.stickycard", "ae2.universalcircuitencoding", "expatternprovider", "gtmqol-early.properties" ]
updated = "2026-11-07 09:57:30"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/EarlyConfig.java"
snippet = "    public static final boolean AE2_STICKY_CARD;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/AE2MixinPlugin.java"
snippet = "    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {"
+++
Matched by name: encoding (early switch), EMI/JEI encoding (only with that viewer), MEStocking*, MEPatternBuffer*, Crafting* (pattern batching and crafting plans, ae2.fast-crafting) and PatternEncodingTermMenuUploadMixin (matrix upload) always, *SharedHatchesMixin (always; shared hatches are toggled per buffer at runtime). Anything else is the sticky card (ae2.stickyCard). ExtendedAE's mod id on 1.20.1 is "expatternprovider" (1.21: "extendedae").