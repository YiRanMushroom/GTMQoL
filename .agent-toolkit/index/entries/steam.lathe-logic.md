+++
title = "Large steam lathe and modular lathe get GTCEu's LatheRecipeLogic by swapping the trait in the constructor"
keys = [ "1.21", "large steam multis", "lubricant", "rod" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/WorkableMultiblockMachineMixin.java"
snippet = "private MachineTrait gtmqol$latheRecipeLogic(WorkableMultiblockMachine self, MachineTrait trait,"
+++
Both branches (1.20.1's GTCEu also has common.machine.trait.LatheRecipeLogic; 1.21.1 compares LATHE_RECIPES.get()). The steam single block lathe has no fluid slot besides steam, so it keeps the plain logic. The same mixin also holds the SAF any-data-hatch change.