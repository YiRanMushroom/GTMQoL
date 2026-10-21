+++
title = "GhostIngredientRecipeViewer: catalyst slots for ghost inputs; empty holders filled with a slot of the right stack class"
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientRecipeViewer.java"
snippet = "public final class GhostIngredientRecipeViewer {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeTypeUILayoutBuilderMixin.java"
snippet = "GhostIngredientRecipeViewer.addDefaults((GTRecipeTypeUILayout.Builder) (Object) this);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeJEICategoryGhostIngredientMixin.java"
snippet = "private void gtmqol$addGhostIngredients(IRecipeLayoutBuilder builder, GTRecipe recipe, IFocusGroup focuses,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTEmiRecipeGhostIngredientMixin.java"
snippet = "private List<EmiIngredient> gtmqol$addGhostIngredientInputs(List<EmiIngredient> inputs) {"
+++
For lookup by the marked thing: JEI gets the marks as invisible catalysts (1.20.1 only mixin; JEI must know the value class, items and fluids), EMI as chance-0 inputs so pattern encoding leaves them out. The JEI mixin targets the full setupRecipeIngredients descriptor, not the generic bridge.