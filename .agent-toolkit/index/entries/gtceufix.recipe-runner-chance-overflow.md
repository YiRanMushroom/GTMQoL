+++
title = "RecipeRunnerMixin: OR/XOR chance roll int overflow above ~215k runs lost chanced outputs; roll in chunks, merge"
keys = [ "1.21", "batch", "chancelogic", "fillcontentmatchlist", "fluidingredient", "gettotalruns", "mspt", "parallel", "sizedingredient" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/RecipeRunnerMixin.java"
snippet = "private List<Content> gtmqol$rollInChunks(ChanceLogic logic, RecipeCapability<?> cap, List<Content> entries,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/RecipeRunnerMixin.java"
snippet = "private static List<Content> gtmqol$merge(List<Content> rolled) {"
+++
Both branches. GTCEu: ChanceLogic OR/XOR int totalChance = times * newChance, times = recipe.getTotalRuns(). The merge matters because NotifiableItemStackHandler.handleRecipe is quadratic in entry count (~10 mspt on ME outputs unmerged). 1.20.1 merges GTCEu 1.20's SizedIngredient (by getInner()) and FluidIngredient (equals ignores amount); IntProviderFluidIngredient, empty fluids and other capabilities stay unmerged. 1.21.1's merge uses NeoForge sized ingredients.