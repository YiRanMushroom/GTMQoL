+++
title = "GhostIngredient<T> with a registered Type; ItemGhostIngredient (left click) and FluidGhostIngredient (right click)"
keys = [ "fromheld" ]
updated = "2026-11-11 07:18:17"
checked = "2026-11-11"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredient.java"
snippet = "public abstract class GhostIngredient<T> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredients.java"
snippet = "GhostIngredient.register(ItemGhostIngredient.TYPE);"
+++
New kinds = a subclass plus a Type registered in GhostIngredients.init (GhostIngredient.register), before anything is decoded; registration order is the order kinds are tried in. List, viewer and EMI only go through the abstract methods.