+++
title = "1.20 pitfall: a class whose static init reaches GTRecipeModifiers must not load in the mod constructor"
kind = "code"
keys = [ "gtrecipemodifiers", "medical_condition", "mod constructor", "pitfall", "registry has been frozen", "static initializer" ]
updated = "2026-11-11 07:39:26"
checked = "2026-11-11"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "// constants only, see ModularMachines"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = "public static final String TOOLTIP_KEY = \"gtmqol.multiblock.modular.tooltip\";"
+++
Calling ModularMachines.addLang() from the GTMQoL constructor failed runData (2026-11-11): "Failed to create mod instance ... [register] registry gtceu:medical_condition has been frozen". ModularMachines has a static GTRecipeModifiers field, GTRecipeModifiers' static init loads GTMedicalConditions, and on 1.20 gtceu's registries aren't open for that while mods are constructed. What works: reading its String constants (javac inlines them, the class isn't loaded) and adding the lang from the constructor itself. On 1.21 the same call is fine (registries are deferred). runData catches this; compileJava doesn't.