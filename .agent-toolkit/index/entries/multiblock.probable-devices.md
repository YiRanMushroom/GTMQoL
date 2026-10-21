+++
title = "Probable Certainty / Improbability Device: multiblock parts that make chanced outputs certain or chanced inputs free"
keys = [ "chanced", "intprovideringredient", "modifyrecipe", "probable certainty device", "probable devices", "probable improbability device", "ranged" ]
updated = "2026-10-19 20:57:50"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/probable/ProbableDevicePartMachine.java"
snippet = "public class ProbableDevicePartMachine extends MultiblockPartMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/PredicatesProbableDeviceMixin.java"
snippet = "private static void gtmqol$probableDevice("
+++
Port of 2.0.0. They work through gtceu's part modifyRecipe hook, which WorkableMultiblockMachine.doModifyRecipe calls after the definition's modifiers, so on the parallel recipe; 2.0.0 cached per recipe id, which would now keep one parallel count. Any multiblock using autoAbilities(recipeTypes) takes them (mixin, preview count 0); structures without it don't. Config machines.probableDevices.