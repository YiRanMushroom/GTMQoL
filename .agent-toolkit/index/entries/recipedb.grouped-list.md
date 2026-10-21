+++
title = "GroupedIngredientList: per-entry kind/group/slot and the canFollow relation used to prune the recipe search"
keys = [ "catalyst", "distinct", "pooled", "shared hatches", "universal", "whole" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/recipedb/GroupedIngredientList.java"
snippet = "public final class GroupedIngredientList extends ObjectArrayList<List<AbstractMapIngredient>> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/recipedb/GroupedIngredientList.java"
snippet = "public static GroupedIngredientList fromHolder(IRecipeCapabilityHolder holder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/recipedb/GroupedIngredientList.java"
snippet = "public boolean canFollow(int from, int to) {"
+++
Only exactly-`RecipeHandlerList` ("plain") RHLs are restricted: another mod's subclass may override handleRecipe and combine its own way, so it is UNIVERSAL. Stricter than 7.x in one way: 7.x let distinct entries combine with the non-distinct pool, but v8's RecipeRunner never accepts that, so pruning loses nothing. Duplicates aren't deduplicated (as upstream). Mirrors recipedb.v8-handler-groups; shares exception: ae2.shared-hatches.