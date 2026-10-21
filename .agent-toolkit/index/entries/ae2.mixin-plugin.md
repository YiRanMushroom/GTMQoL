+++
title = "AE2MixinPlugin: which gtmqol.ae2.mixins.json mixins apply (AE2/EMI/JEI/ExtendedAE present, early config switches)"
keys = [ "ae2.stickycard", "ae2.universalcircuitencoding", "expatternprovider", "gtmqol-early.properties" ]
updated = "2026-10-19 20:12:36"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/EarlyConfig.java"
snippet = "    public static final boolean AE2_STICKY_CARD;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/AE2MixinPlugin.java"
snippet = "    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {"
+++
Matched by name: encoding (early switch), EMI/JEI encoding, MEStocking* and MEPatternBuffer* (always), *SharedHatchesMixin (always; shared hatches are toggled per buffer at runtime). Anything else is the sticky card (ae2.stickyCard). Before 2026-10-20 the shared-hatches mixins not starting with MEPatternBuffer fell into the sticky-card bucket. ExtendedAE's mod id on 1.20.1 is "expatternprovider" (1.21: "extendedae"). EAP mixins use EAPMixinPlugin (ae2.smart-doubling).