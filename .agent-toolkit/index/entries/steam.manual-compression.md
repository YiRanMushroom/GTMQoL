+++
title = "Keep manual 3x3/2x2 compression: every read of GTCEu's disableManualCompression forced to false"
keys = [ "block", "configholder", "nugget", "raw ore", "recipes.keepvanillarecipes", "steam tweaks" ]
updated = "2026-11-17 01:42:46"
checked = "2026-11-17"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ManualCompressionMixin.java"
snippet = "public class ManualCompressionMixin {"
+++
Setting ConfigHolder.INSTANCE.recipes.disableManualCompression = false from our constructor did not work in game, hence ManualCompressionMixin (gated by recipes.keepVanillaRecipes, default on). One mixin, four target classes, each with at least one match. After a GTCEu bump grep GTCEu for disableManualCompression and compare with the method list.