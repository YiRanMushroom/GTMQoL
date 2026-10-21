+++
title = "IntegrationTests: opt-in example single-block and multiblock machines whose assets are generated at runtime"
keys = [ "dynamic resource pack", "integrationtests.enabled", "runtime_multiblock", "runtime_single_block" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (config.integrationTests.enabled && !FMLLoader.getLaunchHandler().isData()) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/test/IntegrationTests.java"
snippet = "public static void registerExampleMachines() {"
+++
Default off; skipped in datagen. Example use of generation.runtime-generation for both machine kinds (GTCEu's textures). In main sources on purpose, to exercise the real dynamic pack lifecycle. Enable integrationTests.enabled in config/gtmqol.yaml, restart, /give @p gtmqol:runtime_single_block and gtmqol:runtime_multiblock, build the 3x3x3 solid-steel-casing shell (hollow centre, 1-2 energy hatches). A test needing a private GTCEu method gets its own test option and must fail loudly.