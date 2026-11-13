+++
title = "GhostIngredientRecipeViewer: catalyst slots for ghost inputs; empty holders filled with a slot of the right stack class"
updated = "2026-11-13 03:40:21"
checked = "2026-11-13"

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
Since 2026-11-12 (the user): inputs go above the progress bar (RecipeViewerWidgetAccess.gtmqol$ghostSlots, a row that GTRecipeViewerWidgetLayoutMixin puts with the progress bar into a column between the input and output columns at buildDefaultLayout's return, only when it has something), one holder per ghost ingredient the recipe has, and each slot gets GHOST_OVERLAY: a grey rect over the stack whose alpha breathes (2.4 s). A column left of the inputs overflowed the recipe viewer (only as wide as GT's layouts) and couldn't be hovered there (1.21 entry has the details).