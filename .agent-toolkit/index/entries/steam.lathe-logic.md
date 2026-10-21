+++
title = "Large steam lathe and modular lathe get GTCEu's LatheRecipeLogic by swapping the trait in the constructor"
keys = [ "large steam multis", "lubricant", "rod" ]
updated = "2026-10-19 20:08:35"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/WorkableMultiblockMachineMixin.java"
snippet = "private MachineTrait gtmqol$latheRecipeLogic(WorkableMultiblockMachine self, MachineTrait trait,"
+++
Both branches (GTCEu's lathe rework, LatheRecipeLogic: lubricant doubles rods). The steam single block lathe has no fluid slot besides steam, so it keeps the plain logic. The same mixin also holds the SAF any-data-hatch change.