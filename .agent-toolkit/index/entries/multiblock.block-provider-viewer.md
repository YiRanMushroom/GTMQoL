+++
title = "Block capability in recipe viewers: catalyst slot via UI layout mixin, JEI invisible catalyst, EMI chance-0 input"
keys = [ "ae encoding", "block capability", "setmaxsize" ]
updated = "2026-10-19 20:12:37"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/blockprovider/BlockProviderRecipeViewer.java"
snippet = "public static void addDefaults(GTRecipeTypeUILayout layout) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeJEICategoryIngredientsMixin.java"
snippet = "public class GTRecipeJEICategoryIngredientsMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTEmiRecipeIngredientsMixin.java"
snippet = "public abstract class GTEmiRecipeIngredientsMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ui/RecipeTypeLayouts.java"
snippet = "public final class RecipeTypeLayouts {"
+++
Recipe types need setMaxSize(IO.IN, CAP, n) to get slots. The JEI mixin (1.20.1 only; JEI is the dev viewer here) adds the block's item as an invisible catalyst so JEI finds the recipe from the block. EMI: chance 0 so AE pattern encoding skips the block (EMI is compile-only on 1.20.1).