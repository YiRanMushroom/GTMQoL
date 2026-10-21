+++
title = "SAF takes 1-9 data access / optical data hatches; any one with the research is enough"
keys = [ "domodifyrecipe", "safdatahatches", "smart assembly factory" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "private static @Nullable MultiPredicate safDataHatches() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/WorkableMultiblockMachineMixin.java"
snippet = "private GTRecipe gtmqol$anyDataHatch(MultiblockPartMachine part, GTRecipe recipe,"
+++
gtceu's dataHatchPredicate() (assembly line) allows exactly one. In doModifyRecipe every part can veto a recipe and a data hatch vetoes what it lacks research for, so with several hatches a recipe would have to be on all of them; the mixin makes it "any data hatch" for the SAF only. Predicate is null when gtceu research is disabled.