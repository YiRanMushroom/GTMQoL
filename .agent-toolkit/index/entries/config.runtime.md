+++
title = "GTMQoLConfig: toma YAML config (config/gtmqol.yaml), every feature toggle, defaults during datagen"
keys = [ "configholder", "configuration", "restart" ]
updated = "2026-11-08 03:57:48"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/GTMQoLConfig.java"
snippet = "public static GTMQoLConfig get() {"
+++
Always read via get() (registers on first use; mixins read it from GTCEu static initializers before our constructor). Datagen gets all defaults, so lang/models/tags of every feature are generated; a toggle only skips runtime registration. Toggles default true and restart-only; integrationTests.enabled defaults false. No restart needed: ae2.patternBufferReturn, ae2.skipNotConsumedInputs, ae2.fastCraftingPlans, ae2.assemblingMatrixFEPerCraft (64), ae2.batchParts (1), misc.autoBuild, voidMiner.maxOperations (64) / maxMultiplier (4096) / dimensionMapping (String[] from=to, empty). Early: config.early.