+++
title = "Gregification lifecycle: sources added in constructor, declared on first RegisterEvent, proxies at common setup"
keys = [ "addsource", "declareall", "foreign recipe types", "gregification core", "gregified" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "modBus.addListener(EventPriority.HIGHEST, RegisterEvent.class, event -> declareAll());"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "private static void declare(ForeignMachineType type) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "Gregification.addSource(MIGregification::types);"
+++
Foreign recipes converted on every reload, nothing in data packs. User's design: one source per mod returns ForeignMachineTypes, rest shared. First RegisterEvent: all mods constructed (MI KubeJS types exist), before our registries and before ModularMachines adds modular_<name>. Skipped in datagen. Then resolveProxy at common setup, conversion per reload (gregification.proxy-mixin), addRecipes.