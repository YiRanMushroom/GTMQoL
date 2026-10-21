+++
title = "IntegrationTests: opt-in example single-block and multiblock machines whose assets are generated at runtime"
keys = [ "dynamic resource pack", "integrationtests.enabled", "runtime_multiblock", "runtime_single_block" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/test/IntegrationTests.java"
snippet = "public static void registerExampleMachines() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (config.integrationTests.enabled && !DatagenModLoader.isRunningDataGen()) {"
+++
Default off. In main sources on purpose, to exercise the real GTCEu runtime and dynamic pack lifecycle (generation.runtime-generation); no hand-written JSON, no datagen output. Enable in config/gtmqol.yaml, then /give @p gtmqol:runtime_single_block and gtmqol:runtime_multiblock and build the 3x3x3 steel-casing structure. No late mixin loader: if a test ever needs a private GTCEu method, put that mixin behind a separate test option and make it fail loudly.