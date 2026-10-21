+++
title = "GhostIngredientListWidget: one row per mark plus an empty row; click/held container/drag from EMI to mark"
keys = [ "ghostingredientslot", "mark ui" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientListWidget.java"
snippet = "public class GhostIngredientListWidget extends Widget<GhostIngredientListWidget>"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientEmi.java"
snippet = "static @Nullable GhostIngredient<?> fromEmi(Object ingredient) {"
+++
Left click marks the held item, right click what the held container holds, empty hand removes. Recipe viewer drags mark items; with EMI fluids too (GhostIngredientEmi converts EMI stacks through ModularUI's converters).