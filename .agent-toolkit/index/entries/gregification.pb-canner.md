+++
title = "CannerBeeLogic: gtceu canner moves bees between cages and spawn eggs, keeping genes and name"
keys = [ "canner_recipes", "entity_data", "productive bees" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/CannerBeeLogic.java"
snippet = "private static final List<String> CAGE_KEYS = List.of(\"entity\", \"name\", \"HivePos\", \"isProductiveBee\", \"mod\");"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/ProductiveBeesGregification.java"
snippet = "() -> GTRecipeTypes.CANNER_RECIPES.get().getCustomRecipeLogicRunners().add(CannerBeeLogic.INSTANCE)));"
+++
Egg ENTITY_DATA = cage tag minus CAGE_KEYS plus id; the cage already dropped UUID/Pos, so genes (NeoForge attachment) and name survive. Reverse: CustomData.loadInto the bee from the egg, then capture into the cage.