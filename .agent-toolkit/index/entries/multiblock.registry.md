+++
title = "GTMQoLMultiblocks: registration and recipes of SAF, DTFR, ILCR, void miner, fishing pond, behind machines.* toggles"
keys = [ "addcyclicmachinelang", "machines config", "magical assembler recipe", "multiblock base", "onregistermachines" ]
updated = "2026-10-21 08:15:00"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "public static void init() {\n        GTMQoLConfig.Machines config = GTMQoLConfig.get().machines;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "private void onRegisterMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "private void onRegisterRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {"
+++
1.20.1: everything registers from GTCEu's RegisterEvents (machines in onRegisterMachines, because initializeAddon runs after the machine registry froze; recipe types in onRegisterRecipeTypes). Electric implosion, crystal chamber and greenhouse have their own classes with initRecipeType/initMachine(s), recipes from GTMQoLAddon.addRecipes. Lang and models are datagen'd. All five GTMQoLMultiblocks controllers (SAF, DTFR, ILCR, void miner, fishing pond) are crafted in the magical assembler.