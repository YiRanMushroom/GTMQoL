+++
title = "Stack-like slots in gtceu's recipe viewer layout: default layout/content builders copied from gtceu's FLUID"
keys = [ "background", "jei", "recipeslotrole", "recipeviewerslotwidget", "stack-like viewers", "stacklikeentrylist" ]
updated = "2026-10-19 20:12:38"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/StackLikeRecipeViewer.java"
snippet = "public static void addDefaults(GTRecipeTypeUILayout.Builder builder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/StackLikeAmountOverlay.java"
snippet = "String s = FormattingUtil.formatBuckets(amount);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeTypeUILayoutBuilderMixin.java"
snippet = "StackLikeRecipeViewer.addDefaults((GTRecipeTypeUILayout.Builder) (Object) this);"
+++
Viewer-agnostic: MUI's RecipeViewerSlotWidget.create(cls) gives a JEI or EMI slot. JEI needs the stack class as a JEI ingredient type (Mek registers its four), EMI needs an EmiStackConverter (stacklike.emi-converter); else empty slots. Gotchas: value(...) resets the background for non-fluids, so set it after; set recipeSlotRole last (the chance setter doesn't rebuild the slot). ContentOverlay only draws fluid amounts, hence StackLikeAmountOverlay.