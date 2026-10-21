+++
title = "GTMQoLConfig: toma YAML config (config/gtmqol.yaml), every feature toggle, defaults during datagen"
keys = [ "configholder", "configuration", "restart" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/GTMQoLConfig.java"
snippet = "public static GTMQoLConfig get() {"
+++
Always read via get() (registers on first use; mixins read it from GTCEu static initializers before our constructor). Datagen gets all defaults, so lang/models/tags for every feature are generated; a toggle only skips runtime registration. All toggles default true and restart-only, except integrationTests.enabled (false) and ae2.patternBufferReturn, ae2.skipNotConsumedInputs, misc.autoBuild (read at runtime). Mixin-apply options: config.early.