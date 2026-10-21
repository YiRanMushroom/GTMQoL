+++
title = "PB industrial beehive: expected hive-trip output per bee, flower requirement, upgrades, custom slot layout"
keys = [ "advanced beehive", "comb block", "isflowerblock", "isfloweritem", "pb_beehive", "productive bees", "productivity upgrade" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/BeehiveLogic.java"
snippet = "enum BeehiveLogic implements GTRecipeType.ICustomRecipeLogic {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/BeehiveLogic.java"
snippet = "private static boolean isFlower(Bee bee, ItemStack stack) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/BeehiveUI.java"
snippet = "final class BeehiveUI {"
+++
Mirrors AdvancedBeehiveBlockEntity.beeReleasePostAction (last matching hive recipe). Bees without a hive recipe (lumber, quarry, dye...) and babies get nothing. Layout not enforced, logic scans every slot. The isFlowerBlock check made time crystal/sculk/silky bees work. Not done: gene sampler and breeding upgrades.