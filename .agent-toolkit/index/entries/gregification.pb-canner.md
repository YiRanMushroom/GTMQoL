+++
title = "CannerBeeLogic: gtceu canner moves bees between cages and spawn eggs, keeping genes and name"
keys = [ "canner_recipes", "entitytag", "productive bees" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/CannerBeeLogic.java"
snippet = "private static final List<String> CAGE_KEYS = List.of(\"entity\", \"name\", \"HivePos\", \"isProductiveBee\", \"mod\");"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/ProductiveBeesGregification.java"
snippet = "() -> GTRecipeTypes.CANNER_RECIPES.getCustomRecipeLogicRunners().add(CannerBeeLogic.INSTANCE)));"
+++
Egg EntityTag = cage NBT minus CAGE_KEYS plus id (the cage already dropped UUID/Pos), so genes and name survive. Reverse: a fresh bee loads the egg's EntityTag over its own data (keeping its UUID, like updateCustomEntityTag without a server level), then is captured into the cage.