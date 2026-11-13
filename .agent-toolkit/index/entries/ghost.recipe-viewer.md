+++
title = "GhostIngredientRecipeViewer: catalyst slots for ghost inputs; empty holders filled with a slot of the right stack class"
keys = [ "emi" ]
updated = "2026-11-13 03:40:21"
checked = "2026-11-13"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientRecipeViewer.java"
snippet = "public final class GhostIngredientRecipeViewer {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ui/RecipeTypeLayouts.java"
snippet = "public final class RecipeTypeLayouts {"
+++
Since 2026-11-12 (the user): inputs go above the progress bar (RecipeViewerWidgetAccess.gtmqol$ghostSlots, a row that GTRecipeViewerWidgetLayoutMixin puts with the progress bar into a column between the input and output columns at buildDefaultLayout's return, only when it has something), one holder per ghost ingredient the recipe has, and each slot gets GHOST_OVERLAY: a grey rect over the stack whose alpha breathes (2.4 s), drawn as the slot's overlay after the slot draws its stack (ModularUI embeds the whole screen in EMI and JEI). A first try put them in a column left of the inputs: the recipe viewer is only as wide as GT's layouts (min 150, its children are relative), so the row overflowed on the left, and the part outside the widget couldn't be hovered (no tooltip). Parent overlays draw before children, so the fade has to be the slot's own overlay.