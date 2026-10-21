+++
title = "PatternBufferIngredients: splits a pattern buffer / proxy RHL into per-worker slots plus CATALYST shared inputs"
keys = [ "circuit slot", "grouped search", "internalslot", "mark pattern outputs", "patternoutputmarks", "shared inventory" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/recipedb/PatternBufferIngredients.java"
snippet = "static boolean addSplit(GroupedIngredientList list, RecipeHandlerList handlerList, int owner) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/recipedb/PatternBufferIngredients.java"
snippet = "private static @Nullable MEPatternBufferPartMachine anyBufferOf(RecipeHandlerList handlerList) {"

[[links]]
path = "src/main/resources/META-INF/accesstransformer.cfg"
snippet = "public com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$Worker slot"
+++
Only called when AE2 is loaded. The marks are added by hand because the buffer RHL's aggregate PatternOutputMarks handler holds every pattern's mark and the split doesn't read the RHL's handler map. Another mod's buffer subclass (e.g. a v8 addon bundling several patterns in one RHL) is not split and stays UNIVERSAL: correct, just not pruned; add a split like this one for it. `workers`/`Worker.slot` are reached via our AT (replaced the earlier accessor mixins).