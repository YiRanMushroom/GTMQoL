+++
title = "Block capability in recipe viewers: catalyst item slot from RecipeTypeLayouts, EMI input with chance 0"
keys = [ "ae encoding", "block capability", "setmaxsize" ]
updated = "2026-11-08 19:26:29"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/blockprovider/BlockProviderRecipeViewer.java"
snippet = "public static void addDefaults(GTRecipeTypeUILayout layout) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTEmiRecipeIngredientsMixin.java"
snippet = "public abstract class GTEmiRecipeIngredientsMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ui/RecipeTypeLayouts.java"
snippet = "public final class RecipeTypeLayouts {"
+++
Recipe types need setMaxSize(IO.IN, CAP, n) to get slots. Chance 0 in EMI so AE pattern encoding skips the block. 1.20.1 also has GTRecipeJEICategoryIngredientsMixin (invisible catalyst).