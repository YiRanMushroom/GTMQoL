+++
title = "1.20.1 datagen gotchas: runData without KubeJS, machines only in onRegisterMachines, old IDEA run config"
keys = [ "does not exit", "failed to create mod instance", "initializeaddon", "kubejsbackgroundthread", "multipleargumentsforoptionexception", "registry frozen", "rejectedexecutionexception", "rundata" ]
updated = "2026-11-09 07:14:11"

[[links]]
path = "build.gradle"
snippet = "modGameRuntime \"dev.latvian.mods:kubejs-forge:${project.kubejs_version}\""

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "private void onRegisterMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {"
+++
1.20.1 only. KubeJS and Rhino are on modGameRuntime (client and server runs only, build.gradle), so runData runs without KubeJS (2026-11-09): KubeJSDataGenFix (its background thread kept the datagen JVM alive) and the flaky "Failed to create mod instance. ModID: kubejs" (RejectedExecutionException in ConsoleJS.resetFile) are gone with it. Machines in IGTAddon.initializeAddon() fail with "registry gtceu:machine has been frozen". Still run the two branches' runData one at a time.