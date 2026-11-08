+++
title = "ModularMachines.declare: gtmqol:modular_<name> registered at the gtceu:machine registry event, runtime-generated"
keys = [ "datagen", "dummy_recipes", "dynamicallygenerated", "generator", "modularmachines.enabled", "pitfall", "recipe type order", "registerevent", "unbound value" ]
updated = "2026-11-08 20:12:18"
checked = "2026-11-08"

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
Models and en_us lang are made at runtime (dynamicallyGenerated), nothing is datagen'd. Other addons' machines are named modular_<ns>_<name>. Skips: no recipe types, DUMMY_RECIPES; a name already taken by another type set logs a warning. Not ported from the old version: the QOL_RECIPE_MODIFIER part ability, non-English names.

1.21 (GTCEu build 139+, #5621): GTCEu registers recipe types after its machine registry, so declare() must not resolve a recipe type holder: type sets are compared by id, and generator/gregified is a lazy Kind (memoized, resolved when the machine runs or its model is generated). ModularMachine overrides isGenerator() and sets regressWhenWaiting from it; the overlay fallback is picked inside the model initializer. Mixed generator/non-generator sets are no longer skipped (can't be told apart in time): they run as processing machines. Resolving at declare time crashed every game load ("Trying to access unbound value: ResourceKey[minecraft:recipe_type / gtceu:electric_furnace]", then GTCEu's own blocks as a cascade). Data runs skip declare(), so runData alone doesn't catch this: see skill.gtceu-upgrade. 1.20 is unaffected (its GTCEu still registers recipe types first).