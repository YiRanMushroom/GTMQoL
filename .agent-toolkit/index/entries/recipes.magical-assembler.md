+++
title = "Magical assembler: always-on recipe type and machines for GTMQoL's own recipes"
keys = [ "circuit conversion", "creative data access hatch", "gtmqol:magical_assembler", "rubber sapling", "simplemachinebuilder" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "RECIPE_TYPE = GTRecipeTypes.register(GTMQoL.id(\"magical_assembler\"), GTRecipeTypes.ELECTRIC)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "public static void addRecipes(Consumer<FinishedRecipe> provider) {"
+++
Always on because most of our recipes (wireless, modular, gregification conversions, multiblocks, AE2 parts, steam machines) use it. Recipe type registered from GTCEu's recipe-type RegisterEvent, machines from the machine RegisterEvent (GTMQoL.onRegisterRecipeTypes / onRegisterMachines). Ported from the old QoLRecipes.kt; the (im)probability devices and industrial LCR were not ported because their outputs no longer exist.