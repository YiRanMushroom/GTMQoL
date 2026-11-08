+++
title = "GhostIngredientRecipeViewer: catalyst slots for ghost inputs; empty holders filled with a slot of the right stack class"
updated = "2026-10-19 20:12:37"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientRecipeViewer.java"
snippet = "public final class GhostIngredientRecipeViewer {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ui/RecipeTypeLayouts.java"
snippet = "public final class RecipeTypeLayouts {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeJEICategoryIngredientsMixin.java"
snippet = "public class GTRecipeJEICategoryIngredientsMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTEmiRecipeIngredientsMixin.java"
snippet = "public abstract class GTEmiRecipeIngredientsMixin {"
+++
For lookup by the marked thing: JEI gets the marks as invisible catalysts (1.20.1 only mixin; JEI must know the value class, items and fluids), EMI as chance-0 inputs so pattern encoding leaves them out. The JEI mixin targets the full setupRecipeIngredients descriptor, not the generic bridge.