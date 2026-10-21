+++
title = "Large steam lathe and modular lathe get GTCEu's LatheRecipeLogic by swapping the trait in the constructor"
keys = [ "1.21", "large steam multis", "lubricant", "rod" ]
updated = "2026-10-21 08:27:56"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/WorkableMultiblockMachineMixin.java"
snippet = "private MachineTrait gtmqol$latheRecipeLogic(WorkableMultiblockMachine self, MachineTrait trait,"
+++
Both branches (1.20.1's GTCEu has common.machine.trait.LatheRecipeLogic; 1.21.1 compares LATHE_RECIPES.get()). Only AdvancedSteamMultiMachine/ModularMachine with LATHE_RECIPES get it. The steam single-block lathe keeps plain RecipeLogic, although SimpleSteamMachineMixin gives it a fluid input tank, so lubricant goes in but makes no extra rods. The same mixin holds the SAF any-data-hatch change.