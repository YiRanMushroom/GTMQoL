+++
title = "ModularMachines.declare: gtmqol:modular_<name> registered at the gtceu:machine registry event, runtime-generated"
keys = [ "datagen", "dummy_recipes", "dynamicallygenerated", "generator", "modularmachines.enabled", "registerevent" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = "private static void declare(Pending pending) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = "if (DatagenModLoader.isRunningDataGen()) return;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/GTMQoLConfig.java"
snippet = "public ModularMachines modularMachines = new ModularMachines();"
+++
Models and en_us lang are made at runtime (dynamicallyGenerated), nothing is datagen'd. Other addons' machines are named modular_<ns>_<name>. Skips (as in the old AddModularMultiblocksLogic.kt): no recipe types, DUMMY_RECIPES, mixed generator/non-generator types; a name already taken by another type set logs a warning. Not ported from the old version: the QOL_RECIPE_MODIFIER part ability, non-English names.