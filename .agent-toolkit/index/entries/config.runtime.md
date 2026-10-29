+++
title = "GTMQoLConfig: toma YAML config (config/gtmqol.yaml), every feature toggle, defaults during datagen"
keys = [ "configholder", "configuration", "restart" ]
updated = "2026-10-21 08:14:03"
checked = "2026-10-29"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/GTMQoLConfig.java"
snippet = "public static GTMQoLConfig get() {"
+++
Always read via get() (registers on first use; mixins read it from GTCEu static initializers before our constructor). Datagen gets all defaults, so lang/models/tags of every feature are generated; a toggle only skips runtime registration. Toggles default true, restart-only, except integrationTests.enabled (false), ae2.patternBufferReturn, ae2.skipNotConsumedInputs, misc.autoBuild (runtime); voidMiner.dimensionMapping: String[] from=to, empty, no restart. Early: config.early.