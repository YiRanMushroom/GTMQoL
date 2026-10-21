+++
title = "The modular multiblock: 3x3x3 any-block shape, x0.125 duration + perfect subtick OC + batch, overlay, recipes"
keys = [ "batch_mode", "getmaxvoltage", "getoverclockvoltage", "gregified", "hammer", "lathe", "latherecipelogic", "magical assembler", "oc_perfect_subtick", "overlay_front" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = ".recipeModifiers(startModifier, overclock, GTRecipeModifiers.BATCH_MODE)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachine.java"
snippet = "public class ModularMachine extends WorkableElectricMultiblockMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = "public static void addRecipes(RecipeOutput provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/WorkableMultiblockMachineMixin.java"
snippet = "(self instanceof AdvancedSteamMultiMachine || self instanceof ModularMachine) &&"
+++
No parallel hatch on purpose: the user only wants subtick parallels. Generators: duration x8 and OC create(0.5, 4.0, true). If every recipe type is gregified (Gregification.allGregified) GregificationModifiers.OVERCLOCK replaces the perfect subtick OC. getMaxVoltage = overclock voltage (old SingleHatchTierSkippingWorkableElectricMachine). The modular lathe gets gtceu's LatheRecipeLogic (lubricant) via WorkableMultiblockMachineMixin.