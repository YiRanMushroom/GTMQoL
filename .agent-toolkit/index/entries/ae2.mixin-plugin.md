+++
title = "AE2MixinPlugin: which gtmqol.ae2.mixins.json mixins apply (AE2/EMI/ExtendedAE present, early config switches)"
keys = [ "ae2.stickycard", "ae2.universalcircuitencoding", "gtmqol-early.properties" ]
updated = "2026-11-08 19:34:14"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/AE2MixinPlugin.java"
snippet = "public class AE2MixinPlugin extends GTMQoLMixinConfigPlugin {"
+++
Only the mixins with a switch or a mod of their own are named (a switch on the simple class name): EncodingHelperMixin (early ae2.universalCircuitEncoding), EmiEncodePatternHandlerMixin (EMI loaded), the sticky card's MEInventoryHandler/NetworkStorage/StorageBusPart mixins (early ae2.stickyCard) and PartSpecialStorageBusMixin (also ExtendedAE, mod id "extendedae"; v7's 1.20 fork used "expatternprovider"). Every other mixin applies: a new one needs no entry here unless it has a switch. Until 2026-11-08 unmatched names counted as sticky card, a trap for new mixins.