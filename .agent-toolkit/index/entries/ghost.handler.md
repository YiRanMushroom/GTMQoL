+++
title = "GhostIngredientHandler + GhostIngredientList: the marks, satisfying ghost inputs without consuming; list UI"
keys = [ "buildlist", "dropped on load", "notifiablerecipehandlertrait" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientHandler.java"
snippet = "public class GhostIngredientHandler extends NotifiableRecipeHandlerTrait<GhostIngredient<?>> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientList.java"
snippet = "public final class GhostIngredientList implements INBTSerializable<ListTag> {"
+++
The list has no length limit; entries whose mod is gone are dropped on load (logged). Clicks and drags arrive as server synced actions (CLICK, SET). Saving needs the handler to be a named persistent trait or a @SaveField field: ghost.saving.