+++
title = "Stack-like slots in gtceu's recipe viewer layout (EMI): default layout/content builders copied from gtceu's FLUID"
keys = [ "background", "recipeslotrole", "recipeviewerslotwidget", "stack-like viewers", "stacklikeentrylist" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/StackLikeRecipeViewer.java"
snippet = "public static void addDefaults(GTRecipeTypeUILayout.Builder builder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeTypeUILayoutBuilderMixin.java"
snippet = "public class GTRecipeTypeUILayoutBuilderMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/StackLikeAmountOverlay.java"
snippet = "String s = FormattingUtil.formatBuckets(amount);"
+++
gtceu: layout builders place named slot widgets, content builders fill them per recipe, linked by GTRecipeViewerWidget.capabilityWidgetName(cap, io, i); gtceu only sets defaults for items/fluids/EU/CWU. Gotchas: value(...) resets the background to SLOT_ITEM for non-fluids, so set background after it; the chance setter doesn't rebuild the EMI slot but recipeSlotRole does, so set the role last. ContentOverlay only draws amounts for SizedFluidIngredient, hence StackLikeAmountOverlay.