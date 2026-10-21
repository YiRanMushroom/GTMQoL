+++
title = "1.20.1 datagen gotchas: KubeJS thread keeps the JVM alive, machines only in onRegisterMachines, old IDEA run config"
keys = [ "does not exit", "initializeaddon", "kubejsbackgroundthread", "multipleargumentsforoptionexception", "registry frozen", "rundata" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (FMLLoader.getLaunchHandler().isData() && ModList.get().isLoaded(\"kubejs\")) {\n            KubeJSDataGenFix.apply();\n        }"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/KubeJSDataGenFix.java"
snippet = "KubeJSBackgroundThread.running = false;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "private void onRegisterMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {"
+++
1.20.1 only. "Capturing errors for startup scripts enabled" in the log is normal; the fix only lets the thread exit. Machines in IGTAddon.initializeAddon() fail with "registry gtceu:machine has been frozen".