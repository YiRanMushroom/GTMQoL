+++
title = "GTRecipeMixin, ParallelLogicMixin: parallels past int (negative total runs, lost chances, saturated inputs)"
kind = "code"
keys = [ "batch", "chanced outputs", "gettotalruns", "input amount", "negative parallel", "overflow", "parallellogic", "room", "saturate", "subtick parallels", "total runs" ]
updated = "2026-11-17 05:06:55"
checked = "2026-11-17"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/GTRecipeMixin.java"
snippet = "private int gtmqol$saturate(int wrapped) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/ParallelLogicMixin.java"
snippet = "private static int gtmqol$withinRoom(int parallelLimit, @Local(argsOnly = true) GTRecipe recipe) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/overclock/Overclocking.java"
snippet = "public static int room(GTRecipe recipe) {"
+++
Both branches. GTCEu: getTotalRuns = parallels * subtickParallels * batchParallels as int; RecipeRunner rolls chanced contents getTotalRuns() times, so a wrapped negative value rolled nothing, and GTMultiblockTextUtil's total runs line showed it negative. Seen with batch mode on top of a big parallel hatch and subtick parallels. Cause: ParallelLogic only keeps the multiplied output amounts within int (limitMaxParallelByOutput: Integer.MAX_VALUE / count); chanced outputs aren't multiplied, voided outputs are skipped, and the input side (getMaxParallelByInput) counts long stock with no int bound, so nothing bounds the product when every output is chanced or voided. The input side also let multiplied input amounts pass int, where ContentModifier.apply(int) saturates them: a run took less than its crafts needed (2 per craft: about half free). Fix: ParallelLogicMixin clamps parallelLimit at the head of getParallelAmount, getParallelAmountWithoutEU and getParallelAmountFast (so every caller: GTCEu's hatch parallel, batchMode, generators, steam multis, our subtick OC, AbsoluteParallel, gregification OC and batch) to Overclocking.room = min(MAX_PARALLELS / crafts so far, Integer.MAX_VALUE / the largest multiplied item or fluid amount: not chanced, not consumed-0, ranged at max roll). GTRecipeMixin saturates getTotalRuns at MAX_PARALLELS (Overclocking.crafts), a fallback for parallels set without ParallelLogic. Saturation alone is not enough: inputs and outputs would stop at int while batch mode still took its full duration. History: 3.9.1 applied the room (crafts only) at each of our call sites; moved into ParallelLogic with the amounts on 2026-11-16, call sites back to MAX_PARALLELS. The int product of a chanced roll's times * chance is gtceufix.recipe-runner-chance-overflow.