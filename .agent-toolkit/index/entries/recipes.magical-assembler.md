+++
title = "Magical assembler: always-on recipe type and machines for GTMQoL's own recipes"
keys = [ "circuit conversion", "creative data access hatch", "gtmqol:magical_assembler", "rubber sapling", "simplemachinebuilder" ]
updated = "2026-10-21 08:15:00"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "RECIPE_TYPE = GTRecipeTypes.register(GTMQoL.id(\"magical_assembler\"), GTRecipeTypes.ELECTRIC)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "public static void addRecipes(Consumer<FinishedRecipe> provider) {"
+++
Always on because most of our recipes (wireless, modular, gregification conversions, multiblocks incl. ILCR, probable devices, AE2 parts, steam machines) use it. Recipe type registered from GTCEu's recipe-type RegisterEvent, machines from the machine RegisterEvent (GTMQoL.onRegisterRecipeTypes / onRegisterMachines). Ported from the old QoLRecipes.kt.