+++
title = "Datagen: Registrate created without listeners, hooked to our mod bus in the constructor (else runData writes nothing)"
keys = [ "addrawlang", "gatherdataevent", "gtregistrate", "langvalue", "registereventlisteners", "src/generated" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "GTMQoLAddon.registrate().registerEventListeners(modBus);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "private static final GTRegistrate REGISTRATE = GTRegistrate.create(GTMQoL.MOD_ID, false);"
+++
Datagen is the default for our resources (lang via .langValue / addRawLang, output committed in src/generated/resources by the MDG `data` run; re-run runData after model/lang changes); runtime generation is opt-in (RuntimeGeneration, generated in RegisterDynamicResourcesEvent). 1.20.1 gotchas: config.datagen-1.20.1.