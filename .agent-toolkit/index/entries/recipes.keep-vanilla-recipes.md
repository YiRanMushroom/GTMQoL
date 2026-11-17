+++
title = "recipes.keepVanillaRecipes: GTCEu's recipe removals skip minecraft: recipes; also keeps manual compression"
kind = "code"
keys = [ "hardglassrecipes", "keepmanualcompression", "keepvanillarecipes", "keepvanillatnt", "nether star", "recipe removal", "removevanillatntrecipe", "tnt" ]
updated = "2026-11-17 01:42:47"
checked = "2026-11-17"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/RecipeRemovalMixin.java"
snippet = "private static Consumer<ResourceLocation> gtmqol$keepVanillaRecipes(Consumer<ResourceLocation> registry) {"
+++
One option (3.9.0) in place of keepVanillaTNT, keepManualCompression and earlyGame's sand -> glass keep; default on, restart. RecipeRemovalMixin wraps the consumer RecipeRemoval.init gets (from GTRecipes.recipeRemoval, which also fills RECIPE_FILTERS) at HEAD and drops every minecraft: id, WoodMachineRecipes.hardWoodRecipes for vanilla wood included. GTCEu's removals of its own recipes (rubber wood) and addons' removeRecipes stay. ManualCompressionMixin follows the same option: GT materials' compression is GTCEu not adding recipes, not a removal, but players see it as the same thing. GTCEu's harder replacement recipes are still added; with GTCEu's defaults none has the same inputs as a kept vanilla one. With nerfWoodCrafting on, vanilla's planks and stick recipes share inputs with GTCEu's nerfed ones and the crafting table picks one. TNT matters for nether stars before IV (recipes.nether-star-dust).