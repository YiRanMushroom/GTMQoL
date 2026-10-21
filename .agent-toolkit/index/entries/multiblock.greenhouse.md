+++
title = "Greenhouse single blocks (all electric tiers) and IV Industrial Greenhouse (16x outputs, dirt in floor)"
keys = [ "crop", "greenhouse", "industrial_output", "machines.greenhouse", "outputmodifier", "sapling" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/greenhouse/Greenhouse.java"
snippet = "private static final RecipeModifier INDUSTRIAL_OUTPUT = (machine, recipe) -> ModifierFunction.builder()"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/greenhouse/Greenhouse.java"
snippet = "INDUSTRIAL = GTMQoLAddon.multiblock(\"industrial_greenhouse\", WorkableElectricMultiblockMachine::new)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/greenhouse/Greenhouse.java"
snippet = "MetaTileEntityLoader.registerMachineRecipe(provider, MACHINES, \"GSG\", \"PMP\", \"WCW\","
+++
Single-block crafting uses GSG/PMP/WCW with a sapling: the fermenter's WPW/GMG/WCW without one collided. Only tiers GT has components for get a recipe. Industrial: IV, with batch mode (the user wants batch mode on our multiblocks). Recipes are plain, with the seed as notConsumable input (distinct lookup inputs, see multiblock.block-provider) + 1000 mB water at VA[LV]. 1.20.1: initRecipeType / initMachines from the RegisterEvents, MachineDefinition[] for registerMachineRecipe.