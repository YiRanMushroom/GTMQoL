+++
title = "recipes.keepVanillaTNT keeps minecraft:tnt crafting regardless of GTCEu's removeVanillaTNTRecipe"
keys = [ "nether star" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/RecipeRemovalMixin.java"
snippet = "private static boolean gtmqol$keepTNT(boolean remove) {"
+++
Part of the "nether stars before IV" goal: TNT feeds GTCEu's implosion compressor (see recipes.nether-star-dust). A mixin because setting gtceu's config from our constructor doesn't stick. Default on.