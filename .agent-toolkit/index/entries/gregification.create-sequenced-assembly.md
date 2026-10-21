+++
title = "Create sequenced assembly line: flattened steps, 3-16 slice line, minimum length per recipe"
keys = [ "create gregification", "length_info", "line too short", "slicerepeatable", "step_limit" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/SequencedAssemblyMachine.java"
snippet = "public static final RecipeModifier STEP_LIMIT = (machine, recipe) -> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/SequencedAssemblySteps.java"
snippet = "public static void register(ResourceLocation recipeType, Step step) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/CreateGregification.java"
snippet = "private static boolean convertSequencedAssembly(RecipeHolder<?> holder, GTRecipeBuilder builder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/MultiblockShape.java"
snippet = "CREATE_ASSEMBLY_LINE("
+++
Length = number of bronze gearbox casings. No separate last slice in the pattern: gtceu's sliceRepeatable is greedy with no backtracking. Results picked by weight via XOR chance; main result is a ghost input. Other mods' step types register a Step; unknown ones use IAssemblyRecipe, 5 s each. Crafted from belt + 16 brass casings + LuV circuit.