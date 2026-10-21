+++
title = "PB industrial beehive: expected hive-trip output per bee, flower requirement, upgrades, custom slot layout"
keys = [ "advanced beehive", "comb block", "isflowerblock", "isfloweritem", "pb_beehive", "productive bees", "productivity upgrade" ]
updated = "2026-10-19 20:12:37"

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
Mirrors PB's advanced beehive. Configurable bees don't count their flower block's item, so isFlowerBlock is asked too; vanilla bees use ItemTags.FLOWERS; fluid flowers checked via the fluid's source block, exact fluid 1000 mB; entity-flower bees need none. Layout not enforced, logic scans every slot. Not done: gene sampler and breeding upgrades.