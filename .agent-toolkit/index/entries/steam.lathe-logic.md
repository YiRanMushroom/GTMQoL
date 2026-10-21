+++
title = "Large steam lathe and modular lathe get GTCEu's LatheRecipeLogic by swapping the trait in the constructor"
keys = [ "large steam multis", "lubricant", "rod" ]
updated = "2026-10-21 08:13:50"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/WorkableMultiblockMachineMixin.java"
snippet = "private MachineTrait gtmqol$latheRecipeLogic(WorkableMultiblockMachine self, MachineTrait trait,"
+++
Both branches (GTCEu's lathe rework, LatheRecipeLogic: 25 mB lubricant gives 2 rods). The steam single-block lathe keeps plain RecipeLogic: registerSimpleSteamMachines takes no recipe logic and the mixin only handles AdvancedSteamMultiMachine/ModularMachine. It does get a fluid input tank from SimpleSteamMachineMixin, but lubricant does nothing there. The same mixin also holds the SAF any-data-hatch change.