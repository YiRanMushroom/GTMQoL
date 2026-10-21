+++
title = "Datagen: Registrate created without listeners, hooked to our mod bus in the constructor (else runData writes nothing)"
keys = [ "addrawlang", "gatherdataevent", "gtregistrate", "langvalue", "registereventlisteners", "src/generated" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "private static final GTRegistrate REGISTRATE = GTRegistrate.create(GTMQoL.MOD_ID, false);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "GTMQoLAddon.registrate().registerEventListeners(modBus);"
+++
GTCEu instantiates addons while GTCEu itself is constructed, so a plain create(MOD_ID) hooked Registrate's GatherDataEvent listener onto GTCEu's mod bus. Datagen is the default for our resources (lang via .langValue / addRawLang, output committed in src/generated/resources; re-run runData after model/lang changes); runtime generation is opt-in, see generation.runtime-generation.