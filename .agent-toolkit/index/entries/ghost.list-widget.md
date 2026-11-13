+++
title = "GhostIngredientListWidget: a row per mark plus an empty row; click, drag or type an ID to mark"
keys = [ "ghostingredientslot", "mark by id", "mark ui", "type an id" ]
updated = "2026-11-13 05:02:28"
checked = "2026-11-13"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientListWidget.java"
snippet = "public class GhostIngredientListWidget extends Widget<GhostIngredientListWidget>"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientListWidget.java"
snippet = "public @Nullable Object castGhostIngredientIfValid(Object ingredient) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientEmi.java"
snippet = "static @Nullable GhostIngredient<?> fromEmi(Object ingredient) {"
+++
Left click marks the held item, right click what the held container holds, empty hand removes. The ghost slot takes Object: JEI (the 1.20.1 dev viewer) hands over its own item/fluid stacks via castGhostIngredientIfValid -> GhostIngredient.ofValue; EMI goes through ingredientHandlingOverride and GhostIngredientEmi (ModularUI's EMI converters), only when EMI is loaded. EMI is compile-only on 1.20.1.

Left click empty-handed on the empty (+) row opens GhostIdPanel (client-only IPanelHandler.simple sub panel), for players without a recipe viewer: a text field and one Add button per GhostIngredient.typeable() kind (Type.idReader non-null; tag kinds pass null, marks are single items and fluids): Add as item, Add as fluid. Each button shows the icon of what it would add and that thing's tooltip; while the id names nothing of its kind it is faded (a translucent grey rect over it, grey text; MUI buttons have no disabled state) and ignores clicks. "No item or fluid has this ID" shows when neither matches. Enter adds only when exactly one kind matches. GhostIngredient.parseId lowercases and parses the text. The mark goes through the list's SET synced action, appended at the end, and the popup closes.