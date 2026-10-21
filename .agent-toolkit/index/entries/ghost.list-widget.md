+++
title = "GhostIngredientListWidget: one row per mark plus an empty row; click/held container/drag from JEI or EMI to mark"
keys = [ "ghostingredientslot", "mark ui" ]
updated = "2026-10-19 20:12:37"

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