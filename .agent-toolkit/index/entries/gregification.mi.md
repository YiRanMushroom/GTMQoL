+++
title = "Modern Industrialization source: 14 single blocks + 10 multiblocks, energy-preserving converter, coil temps"
keys = [ "blast furnace", "guidebook", "machinerecipe", "mi_", "mimachinerecipetypes", "modern industrialization", "widentogt" ]
updated = "2026-11-13 03:48:58"
checked = "2026-11-13"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mi/MIGregification.java"
snippet = "public static List<ForeignMachineType> types() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mi/MIGregification.java"
snippet = "private static boolean convert(RecipeHolder<?> holder, GTRecipeBuilder builder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mi/MIGregification.java"
snippet = "private static Ingredient widenToGT(Ingredient ingredient) {"

[[links]]
path = "gradle.properties"
snippet = "mi_version=HOR1tVas"
+++
1.21 only: MI on 1.20.1 is Fabric-only (NeoForge from 1.20.4, no Forge port); Sinytra Connector would need rewriting the converter for Fabric fluid API. Config gregification.modernIndustrialization. MI fluid->GT material is a name guess only. Blast furnace UI shows GT's coil line (TEMP_COIL_INFO). HOR1tVas = MI 2.5.10.

Multiblock slot counts are DynamicRecipeSlots.ANY (recipeviewer.dynamic-slots): each recipe shows its own slots. Why: MI's recipes aren't in its jar (generated at runtime), and the dev log (2026-11-12) showed quarry recipes with up to 10 outputs and the distillation tower with 8, past the fixed 6 (GT warns once per output past the maximum). MI itself (checked 2026-11-12) picks between recipes with the same inputs by slot locking: CrafterComponent.updateActiveRecipe starts the first recipe whose outputs fit (canStartRecipe), sets matchesMultipleRecipes when another overlapping one also fits (the window warns "The inputs match more than one possible recipe. Lock all output slots..."), and lockRecipe (the recipe viewer's + button with the window open) locks the slots to one recipe. GT machines have no slot locks, hence the ghost marks. MI energy (not ours: gregified machines use GT EU): modern_industrialization-startup.toml [compat] bidirectionalEnergyCompat, default false = MI only pushes into FE blocks (InsertOnlyExternalStorage, forgeEnergyPerEu 10 in the server config); true = MI blocks also expose FE both ways, which packs turn on. Marks follow the product: a fluid output toGT turns into GT's is marked by the GT material's fluid tag (ghost.tags). MARKED single blocks (assembler, distillery, mixer) go through MarkCoveredRecipes: the distillery has one recipe per product of the same fluid, the mixer's he/le MOX and uranium recipes differ only in amounts, and a recipe whose inputs are a prefix of another's (purple dye) can't enter GT's lookup at all ("without exact duplicate/conflict"); without marks GT dropped 23 of them.