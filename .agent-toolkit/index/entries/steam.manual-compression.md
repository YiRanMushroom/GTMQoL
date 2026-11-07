+++
title = "Keep manual 3x3/2x2 compression: every read of GTCEu's disableManualCompression forced to false"
keys = [ "block", "configholder", "keepmanualcompression", "nugget", "raw ore", "recipes.keepmanualcompression", "steam tweaks" ]
updated = "2026-11-07 10:20:49"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ManualCompressionMixin.java"
snippet = "public class ManualCompressionMixin {"
+++
Setting ConfigHolder.INSTANCE.recipes.disableManualCompression = false from our constructor did not work in game, hence the mixin, gated by recipes.keepManualCompression (default on, restart). One mixin, four target classes (RecipeAddition, RecipeRemoval, MaterialRecipeHandler, OreRecipeHandler), each with at least one match. After a GTCEu bump grep GTCEu for disableManualCompression and compare with the method list.