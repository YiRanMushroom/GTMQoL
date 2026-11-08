+++
title = "AE2MixinPlugin: which gtmqol.ae2.mixins.json mixins apply (AE2/EMI/JEI/ExtendedAE present, early config switches)"
keys = [ "ae2.stickycard", "ae2.universalcircuitencoding", "expatternprovider", "gtmqol-early.properties" ]
updated = "2026-11-08 19:34:14"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/AE2MixinPlugin.java"
snippet = "public class AE2MixinPlugin extends GTMQoLMixinConfigPlugin {"
+++
Only the mixins with a switch or a mod of their own are named (a switch on the simple class name): EncodingHelperMixin (early ae2.universalCircuitEncoding), EmiEncodePatternHandlerMixin / EncodePatternTransferHandlerMixin (only with that viewer), the sticky card's MEInventoryHandler/NetworkStorage/StorageBusPart mixins (early ae2.stickyCard) and PartSpecialStorageBusMixin (also ExtendedAE, whose 1.20.1 mod id is "expatternprovider"; 1.21: "extendedae"). Every other mixin applies: a new one needs no entry unless it has a switch. Until 2026-11-08 unmatched names counted as sticky card.