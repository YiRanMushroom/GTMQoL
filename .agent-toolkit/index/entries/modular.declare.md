+++
title = "ModularMachines.register: gtmqol:modular_<name> registered at once, nested in the single block's register()"
keys = [ "1.21", "datagen", "dummy_recipes", "dynamicallygenerated", "generator", "modularmachines.enabled", "runtime generation" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = "public static void register(MachineDefinition simple) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = "if (FMLLoader.getLaunchHandler().isData()) return;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = ".dynamicallyGenerated(true)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/GTMQoLConfig.java"
snippet = "public ModularMachines modularMachines = new ModularMachines();"
+++
Runs inside gtceu's machine registry event (definitions are eager here, unlike 1.21's queue + declare at the registry event). Models and en_us lang at runtime, nothing datagen'd; skipped in datagen. Other addons: modular_<ns>_<name>. Skips: no recipe types, DUMMY_RECIPES, mixed generator/non-generator types; a name taken by another type set logs a warning. Config is GAME_RESTART. Not ported from the old version: QOL_RECIPE_MODIFIER part ability, non-English names.