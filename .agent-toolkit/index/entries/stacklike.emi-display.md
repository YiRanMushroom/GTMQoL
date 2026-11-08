+++
title = "Stack-like slots in gtceu's recipe viewer layout: default layout/content builders copied from gtceu's FLUID"
keys = [ "background", "jei", "recipeslotrole", "recipeviewerslotwidget", "stack-like viewers", "stacklikeentrylist" ]
updated = "2026-10-19 20:12:38"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/StackLikeRecipeViewer.java"
snippet = "public static void addDefaults(GTRecipeTypeUILayout layout) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/StackLikeAmountOverlay.java"
snippet = "String s = FormattingUtil.formatBuckets(amount);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ui/RecipeTypeLayouts.java"
snippet = "public final class RecipeTypeLayouts {"
+++
Viewer-agnostic: MUI's RecipeViewerSlotWidget.create(cls) gives a JEI or EMI slot. JEI needs the stack class as a JEI ingredient type (Mek registers its four), EMI needs an EmiStackConverter (stacklike.emi-converter); else empty slots. Gotchas: value(...) resets the background for non-fluids, so set it after; set recipeSlotRole last (the chance setter doesn't rebuild the slot). ContentOverlay only draws fluid amounts, hence StackLikeAmountOverlay.