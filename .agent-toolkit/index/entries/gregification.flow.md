+++
title = "Gregification lifecycle: sources in constructor, types on GTCEu's recipe-type event, machines on its machine event"
keys = [ "addsource", "foreign recipe types", "gregification core", "gregified", "onregistermachines", "onregisterrecipetypes", "registerevent" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "Gregification.addSource(MekanismGregification::types);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "modBus.addGenericListener(GTRecipeType.class, Gregification::onRegisterRecipeTypes);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "private static GregifiedRecipeType declareRecipeType(ForeignMachineType type) {"
+++
Foreign recipes converted on every reload, nothing in data packs. One source per mod returns ForeignMachineTypes (user's design), rest shared. Sources (Mek, Create, PB; each behind config gregification.* + ModList) are read on GTCEu's GTRecipeType RegisterEvent (posted from its common proxy, after all mods constructed); machines declared on the later MachineDefinition event. Skipped in datagen. Proxies resolved at common setup (gregification.recipe-type). 1.21 uses a different event order.