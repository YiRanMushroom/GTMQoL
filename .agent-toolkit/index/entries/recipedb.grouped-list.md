+++
title = "GroupedIngredientList: per-entry kind/group/slot and the canFollow relation used to prune the recipe search"
keys = [ "catalyst", "distinct", "pooled", "shared hatches", "undyed", "universal", "whole" ]
updated = "2026-10-19 20:12:37"

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
Only exactly-RecipeHandlerList ("plain") RHLs are restricted: another mod's subclass may override handleRecipe and combine its own way, so it is UNIVERSAL. Stricter than 7.x: 7.x let distinct entries combine with the non-distinct pool, but v8's RecipeRunner never accepts that, so nothing is lost. Only exception: a pattern buffer slot with Shared Hatches on may follow undyed entries. Duplicate handlers aren't deduplicated (same as upstream). Rules mirrored: recipedb.v8-handler-groups.