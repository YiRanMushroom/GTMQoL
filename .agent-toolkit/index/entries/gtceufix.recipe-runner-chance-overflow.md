+++
title = "RecipeRunnerMixin: OR/XOR chance roll int overflow above ~215k runs lost chanced outputs; roll in chunks, merge"
keys = [ "batch", "chancelogic", "fillcontentmatchlist", "gettotalruns", "mspt", "parallel" ]
updated = "2026-11-17 03:31:47"
checked = "2026-11-17"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/RecipeRunnerMixin.java"
snippet = "public abstract class RecipeRunnerMixin {"
+++
Both branches. A negative number of runs (the three parallels multiplied as ints) is gtceufix.total-runs-overflow. GTCEu source: ChanceLogic OR/XOR `int totalChance = times * newChance`, times = recipe.getTotalRuns(). The merge matters because NotifiableItemStackHandler.handleRecipe is quadratic in entry count (~10 mspt on ME outputs unmerged). Ranged (IntProvider) outputs and other capabilities are not merged. 1.20.1's merge is a Forge rewrite (GTCEu 1.20 SizedIngredient/FluidIngredient).