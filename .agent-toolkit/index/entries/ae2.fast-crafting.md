+++
title = "Our pattern batching and crafting planner: batches cut from the plan, topological plans, no EAP, unit tested"
kind = "design"
keys = [ "ae2", "assembling matrix", "crafting plan", "pattern batch", "pattern batching", "topological planner" ]
updated = "2026-11-15 22:55:56"
checked = "2026-11-15"

[[items]]
name = "solver"
value = "Plain Java planner"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/crafting/CraftingSolver.java"
snippet = "public final class CraftingSolver"

[[items]]
name = "batches"
value = "Cuts a plan's crafts"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/crafting/BatchPlan.java"
snippet = "public final class BatchPlan"

[[items]]
name = "scaled-pattern"
value = "A batch and its hidden item"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/crafting/PatternBatch.java"
snippet = "public final class PatternBatch implements IPatternDetails {"

[[items]]
name = "calculation"
value = "Tries the planner first"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCalculationPlannerMixin.java"
snippet = "public abstract class CraftingCalculationPlannerMixin"

[[items]]
name = "partial-push"
value = "AE2's CPU pushes part of a batch it can't push whole"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCpuLogicBatchMixin.java"
snippet = "public abstract class CraftingCpuLogicBatchMixin"

[[items]]
name = "partial-count"
value = "how many crafts can go now, unit tested"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/crafting/PartialPush.java"
snippet = "public final class PartialPush"
+++
User's decisions, 2026-11-06/07. Both act before any CPU sees a plan, so every CPU gets them. Mixins on AE2's flow: plan (buildCraftingPlan), getProviders, the calculation's start, optional CPU budget; none on patterns.

## Testing
CraftingSolver and BatchSizes hold no game classes: gradlew test plays every plan out as a CPU would, on random worlds, against a copy of AE2's walk. The AE2 side is only compile checked: a null key reaching a decoder crashed the first world load (GT decodes empty slots). Mixins can be checked without the game: Class.forName(name, false, loader) in init, then runData.

## Doubling
A pattern is batched only if every provider of it is ours (PatternBatchProvider). Patterns that make or give back an input stay single: AE2 plans those so the same items go round, and a batch would wait forever. All crafts of a pattern are one batch, or as few as the providers' limit allows (BatchSizes.pieces). Until 2026-11-15 crafts that wait for inputs or power were cut 1, 2, 4.. and the rest (config ae2.patternBatchParts); the user had it removed: the partial push (Push time) does that better on AE2's CPU and the pieces blocked it (repeated sizes), and on other CPUs the last piece was still half the order, so chains ran 1.5x instead of 2x serial and power only helped up to twice the storage. Don't bring it back. Definition = hidden item + decoder, so a job comes back from a restart as it was on any CPU (EAP loses the multiplier there). A CPU budget mixin counts one pattern's pieces once, AE2's CPU only.

## Push time
Only AE2's own CPU (CraftingCpuLogicBatchMixin), which the ME Data Center uses too; on other CPUs a batch waits for all its inputs and power (chains run step after step; stuck when it needs more AE power than the network stores), accepted by the user. A job takes all the stock it needs into the CPU when it starts (tryExtractInitialItems), so no other job can take it later; what a batch can lack at push time is items still being made upstream, and AE power. When the whole batch can't go, the CPU pushes the most crafts that can: the inputs by a copy of AE2's extraction (PartialPush, unit tested), the power in proportion, then the real extraction and power check decide. The rest becomes a piece of its own, saved like any other. This also ends the stall of a batch needing more power than the network can store. Only for a pattern's last piece of that size, or when the rest's size is already a task: AE2's task counter can't be made from outside (its constructor is package private and Mixin can't make one without naming the type), so the finished piece's counter is reused. Repeated sizes come from a provider's limit and then wait as AE2 would.

## Planner
Choose by priority (AE2 does fall back to the next pattern; equal priority is arbitrary), leave out recipes that loop (as notRecursive), order items, one walk of exact totals. A shortage moves crafts to the next recipe of the nearest choice above; a higher change resets lower ones; max 300 walks. Ends missing without asking AE2: the user's call, this is NP-like, rules of thumb are fine, and shortage with many patterns is to be avoided anyway. Refused, so AE2 decides: anything that gives back, a looped recipe or substitutes when short, a wanted byproduct that can't be counted. On random worlds: AE2's plan with one recipe per item or when first recipes suffice; with choices 1.2% of AE2's plans are missed, 30% more found.

## Byproducts
Two orders: adding up needs the maker before the byproduct, time needs inputs before their recipe. A byproduct feeding its own recipe's ingredients adds up fine but deadlocks the CPU (found by the test that plays plans out), so a loop in time stops it counting.

## Rejected
Search on amounts: per node doubling multiplies with nesting, root search needed exceptions through AE2's loop. Pieces of 1, 2, 4.. in the plan (see Doubling). AE2's time slice: EAP's speedup is only a bigger slice, costing server time, not copied.