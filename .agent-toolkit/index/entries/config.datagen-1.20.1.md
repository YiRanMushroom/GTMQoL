+++
title = "1.20.1 datagen gotchas: KubeJS thread keeps the JVM alive, machines only in onRegisterMachines, old IDEA run config"
keys = [ "does not exit", "failed to create mod instance", "initializeaddon", "kubejsbackgroundthread", "multipleargumentsforoptionexception", "registry frozen", "rejectedexecutionexception", "rundata" ]
updated = "2026-11-08 21:58:01"
checked = "2026-11-08"

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
1.20.1 only. "Capturing errors for startup scripts enabled" in the log is normal; the fix only lets the thread exit. Machines in IGTAddon.initializeAddon() fail with "registry gtceu:machine has been frozen". "Failed to create mod instance. ModID: kubejs" with a RejectedExecutionException in ConsoleJS.resetFile (KubeJS's log executor already terminated) is not our code: seen 2026-11-08 four runs in a row after a runData run alongside the 1.21 one, the same with our change disabled; a run of the committed tree passed and the next ones did too. Retry, run the two branches' runData one at a time.