+++
title = "overclocking.absoluteParallelHatches: parallel hatches at the EU/t of one recipe, not for generators"
keys = [ "parallelhatchesareabsolute" ]
updated = "2026-10-21 08:08:19"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/HatchParallelMixin.java"
snippet = "private static void gtmqol$absolute(MetaMachine machine, GTRecipe recipe,"
+++
2.0.0's addonConfig.parallelHatchesAreAbsolute (default on), back under overclocking. Replaces gtceu's hatchParallel (getParallelAmount + eutMultiplier) with AbsoluteParallel.upTo(hatch parallels), which caps by inputs only and leaves EU/t; the OC modifiers after it then see one recipe's EU/t. Separate from GTRecipeModifiersMixin so the OC overhaul switch doesn't skip it.