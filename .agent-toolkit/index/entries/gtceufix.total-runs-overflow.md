+++
title = "GTRecipeMixin: getTotalRuns (int product of the three parallels) wrapped negative above 2^31: chanced outputs lost"
kind = "code"
keys = [ "batch", "chanced outputs", "gettotalruns", "negative parallel", "overflow", "subtick parallels", "total runs" ]
updated = "2026-11-17 03:32:08"
checked = "2026-11-17"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/GTRecipeMixin.java"
snippet = "private int gtmqol$saturate(int wrapped) {"
+++
Both branches. GTCEu: getTotalRuns = parallels * subtickParallels * batchParallels as int; RecipeRunner rolls chanced contents getTotalRuns() times, so a wrapped negative value rolled nothing, and GTMultiblockTextUtil's total runs line showed it negative. Seen with batch mode on top of a big parallel hatch and subtick parallels. Two parts: the mixin saturates getTotalRuns at Overclocking.MAX_PARALLELS (Overclocking.crafts), and every parallel stage we control is limited by Overclocking.room so the product stays within it (subtick OC in OverclockingLogicMixin, GTCEu batchMode via GTRecipeModifiersMixin, AbsoluteParallel, GregificationModifiers overclock and batch). The saturation alone would leave inputs and outputs capped at int while the batch still took its full duration. The int product of a chanced roll's times * chance is gtceufix.recipe-runner-chance-overflow.