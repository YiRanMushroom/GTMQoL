+++
title = "GTMQoLConfig: toma YAML config (config/gtmqol.yaml), every feature toggle, defaults during datagen"
keys = [ "configholder", "configuration", "restart" ]
updated = "2026-11-07 09:56:23"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/GTMQoLConfig.java"
snippet = "public static GTMQoLConfig get() {"
+++
Always read via get() (registers on first use; mixins read it from GTCEu static initializers before our constructor). Datagen gets all defaults, so lang/models/tags of every feature are generated; a toggle only skips runtime registration. Toggles default true and are restart-only (UpdateRestrictions.GAME_RESTART), except integrationTests.enabled (default false). No restart: ae2.patternBufferReturn, ae2.skipNotConsumedInputs, ae2.patternMatrixFEPerCraft, ae2.batchParts, ae2.fastCraftingPlans, misc.autoBuild, voidMiner.* (dimensionMapping: String[] from=to, empty). Early: config.early.