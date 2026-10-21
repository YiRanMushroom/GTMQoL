+++
title = "PatternBufferIngredients: splits a pattern buffer / proxy RHL into per-worker slots plus CATALYST shared inputs"
keys = [ "1.21", "circuit slot", "grouped search", "internalslot", "mark pattern outputs", "patternoutputmarks", "shared inventory", "stack-like" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/recipedb/PatternBufferIngredients.java"
snippet = "static boolean addSplit(GroupedIngredientList list, RecipeHandlerList handlerList, int owner) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/recipedb/PatternBufferIngredients.java"
snippet = "private static @Nullable MEPatternBufferPartMachine anyBufferOf(RecipeHandlerList handlerList) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/recipedb/MEPatternBufferPartMachineAccessor.java"
snippet = "List<?> gtmqol$getWorkers();"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/recipedb/MEPatternBufferWorkerAccessor.java"
snippet = "MEPatternBufferPartMachine.InternalSlot gtmqol$getSlot();"
+++
Only called with AE2. Splits gtceu's buffer, our AbstractMEPatternBufferPartMachine subclasses and gtceu's proxy; another mod's subclass stays whole/UNIVERSAL (correct, not pruned). Output marks added by hand: the split doesn't read the RHL's handler map. Workers also get their stack-like (AEStackLikeBridges) contents. workers/Worker.slot via accessor mixins in the recipedb config (1.21.1 uses an AT); the ae2 config has its own slot accessor.