+++
title = "GhostIngredient<T> with a registered Type; ItemGhostIngredient (left click) and FluidGhostIngredient (right click)"
keys = [ "fromheld" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredient.java"
snippet = "public abstract class GhostIngredient<T> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/ItemGhostIngredient.java"
snippet = "public static final Type<ItemStack> TYPE = register(new Type<>(\"item\", ItemStack.class,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/FluidGhostIngredient.java"
snippet = "public static final Type<FluidStack> TYPE = register(new Type<>(\"fluid\", FluidStack.class,"
+++
New kinds = a subclass plus a registered Type; list, viewer and EMI only go through the abstract methods. Types register on class load, so GhostIngredients.init touches each TYPE before anything is decoded.