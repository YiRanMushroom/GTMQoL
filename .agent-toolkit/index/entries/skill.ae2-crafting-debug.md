+++
title = "Debugging AE crafting, batches and the Assembling Matrix: a job's path, saved state, symptoms; use when a job misbehaves"
kind = "howto"
keys = [ "assembling matrix bug", "batch debugging", "crafting job stuck" ]
updated = "2026-11-08 05:04:08"
checked = "2026-11-08"

[[items]]
name = "plan"
value = "Step 1: our planner before AE2's tree"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCalculationPlannerMixin.java"
sha256 = "425338a2fc729372afc155d8b7bb2fc8276f6bc1c2e949d0e89c1ab86cf41697"

[[items]]
name = "cut"
value = "Step 2: batches cut from the plan"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingSimulationStateBatchMixin.java"
sha256 = "c3485f30b45b62368cf9b876cf3e8a270623ab67ef9f16353bc5314748ba0938"

[[items]]
name = "push"
value = "Step 5: one operation per pattern, partial push"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCpuLogicBatchMixin.java"
sha256 = "52ab37051e43d42fff5f9e9deafe2cd09a43c09d9233a97a055239164c5a4439"

[[items]]
name = "interface"
value = "Step 6: matrix side"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixInterfaceMachine.java"
sha256 = "04baa7988778489b45c10fae0d764fdba800db75f78fed5a9980593cb75579c3"

[[items]]
name = "work"
value = "Step 7 and the matrix's saved state"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixWork.java"
sha256 = "0f0da895d7a1e11a817a74e535aa4184ff1f10306f4f25c02610ba802900b001"

[[items]]
name = "batch"
value = "Saved batches: hidden Pattern Batch item and decoder"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/batch/PatternBatch.java"
sha256 = "7c30511f1fa8cfadba455153fc6c79bb82783ef7de3d1949f0ae0e3bcdee420b"
+++
For when something goes wrong in game. Why things are as they are: ae2.fast-crafting (planner, batches, push time), ae2.pattern-matrix, ae2.pattern-batching. Class names below are in integration/ae2/batch, integration/ae2/matrix and core/mixins/ae2.

## A job's path
1. **Plan** (CraftingCalculationPlannerMixin, HEAD of runCraftAttempt, config ae2.fastCraftingPlans): CraftingSolver over AECraftingPlanner. PLANNED: AE2's buildCraftingPlan builds the plan from our simulated inventory. MISSING (also after MAX_WALKS = 300 walks): the preview shows what is missing; a real order gets no plan. REFUSED: AE2's own tree runs as if we weren't there (give-backs, loops that matter, substitutes when short).
2. **Cut** (CraftingSimulationStateBatchMixin, HEAD of buildCraftingPlan -> BatchPlan.cut): a pattern whose providers are all PatternBatchProvider becomes PatternBatch batches: whole when its inputs come from stock and AE power is there, else BatchSizes.split (1, 2, 4.. and the rest; ae2.patternBatchParts). Limit = smallest batchLimit of its providers.
3. **Submit** (AE2): the CPU takes all stock the plan uses into itself (tryExtractInitialItems); no other job can take it after.
4. **Providers** (CraftingServiceBatchMixin): getProviders unwraps a PatternBatch, so the original's providers are asked.
5. **Push** (AE2's CraftingCpuLogic.executeCrafting + CraftingCpuLogicBatchMixin, AE2's CPU only): a pattern's batches in one pass cost one operation; a last piece of its size that can't go whole pushes the part inputs and power allow (PartialPush), the rest goes back as its own task after the loop. AE2 adds the pushed pattern's outputs to waitingFor; for a part they are the part's.
6. **Provider side**: pattern buffer (MEPatternBufferPatternBatchingMixin, unwraps, pushes the batch's inputs) or AssemblingMatrixInterfaceMachine.pushPattern (unwraps, AssemblingMatrixMachine.push: catalysts first, then AssemblingMatrixWork.add).
7. **Matrix work, every tick** (AssemblingMatrixWork.tick): first giving-back runs (all or nothing), then each queued pattern once, up to capacity, paid in FE (assemblingMatrixFEPerCraft); products into `outputs`, flushed to the network through the interface each tick. The CPU takes what it waits for first (AE2 gives a CPU's insert top priority).

## What is saved
- CPU tasks: by pattern definition. A batch is the hidden item pattern_batch (original + multiplier), decoded back by PatternBatch's decoder, so restarts keep sizes. EAP's pattern batches lose theirs (not ours).
- Matrix (controller, field `work`): queue per pattern definition with crafts, outputs and the pushed inputs per input slot (key, amount per use, amount left; `withInputs` = crafts that have inputs; older saves have none); giving-back runs (outputs, inputs, paid); `outputs` not yet taken. Unformed: kept and paused. Controller broken: giveBackAll sends inputs of undone crafts and outputs to the network, the rest is gone.

## Switches to narrow a bug down
- ae2.fastCraftingPlans=false: AE2 plans alone; compare plans.
- Pattern buffer x2 off: that buffer's patterns stay single. A pattern also in a plain pattern provider is never batched (limit 0).
- Matrix batches always, except our patterns whose catalyst wears (batchLimit 0).

## Symptom -> where to look
- **Job never ends, CPU waits for items**: matrix unformed (paused), "Not enough FE", "Waiting for the network" (network full), Shift cancel or a broken controller while the job ran, or a waitingFor mismatch from the partial push (step 5).
- **Order says missing, AE2 would craft it**: planner MISSING (choices: about 1.2% of AE2's plans missed in tests); retry with fastCraftingPlans=false.
- **Plan takes long**: MAX_WALKS, or REFUSED and AE2's own tree is slow.
- **No batches**: a provider that isn't ours offers the pattern, the pattern makes or gives back one of its inputs (BatchPlan.canBatch), x2 off, catalyst wears.
- **Batch waits forever**: needs more AE power than stored on a CPU that isn't AE2's, or a repeated size (x2 limit, patternBatchParts > 1) that the partial push skips.
- **Too many CPU operations**: the one-operation rule is only on AE2's CPU.
- **Pattern in the matrix but not offered**: only AE2 crafting/stonecutting/smithing (IMolecularAssemblerSupportedPattern) and our crafting patterns; equal patterns count once; needs a formed matrix with its interface.
- **Auto upload doesn't take it**: switched off, no free slot, the same pattern already there (then the copy becomes a blank), processing patterns never; PatternEncodingTermMenuUploadMixin runs before ExtendedAE Plus (priority 500).
- **Wrong amounts back on cancel**: per-slot accounting in Queued/Slot; crafts from saves before it give nothing back.
- **Crash on world load in a decoder**: a null key (GT's buffer decodes empty slots); decoders check for null.
- **EAP installed**: its CraftingCpuLogicPatternPowerMixin also hooks executeCrafting (only its own pattern batches).

## Checking without the game
Unit tests (gradlew test, integration/ae2/batch): TestWorld plays plans out as a CPU would; PartialPush against trying every count. Mixins: a temporary Class.forName(name, false, loader) in PatternBatch.init, then runData, then remove it.

## On 1.20
Same classes and flow. Differences (build.branch-differences): AE2 15, so getOutputs() is an array; NBT instead of data components; no registries in serialization; the pattern batch item has no hidden-from-recipe-viewers tag.
