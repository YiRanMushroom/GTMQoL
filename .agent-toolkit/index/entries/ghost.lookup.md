+++
title = "GhostMapIngredient lookup node; getDefaultMapIngredient must return a mutable list (gtceu pools in place)"
keys = [ "applypooling", "list.of", "mapingredientpool", "unsupportedoperationexception" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientRecipeCapability.java"
snippet = "// mutable: GT's recipe lookup pools these in place (MapIngredientPool.applyPooling sets list elements)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostMapIngredient.java"
snippet = "public final class GhostMapIngredient extends AbstractMapIngredient {"
+++
With List.of, world loading crashed (UnsupportedOperationException) as soon as two recipes had the same mark: GT's MapIngredientPool.applyPooling sets list elements in place. BlockProviderRecipeCapability returns a mutable list for the same reason (multiblock.block-provider).