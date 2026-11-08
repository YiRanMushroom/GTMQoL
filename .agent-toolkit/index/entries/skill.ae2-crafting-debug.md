+++
title = "Debugging AE crafting, batches and the Assembling Matrix: a job's path, saved state, symptoms; use when a job misbehaves"
kind = "howto"
keys = [ "assembling matrix bug", "batch debugging", "crafting job stuck", "lost items", "waitingfor" ]
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
name = "providers"
value = "Step 4: getProviders unwraps batches"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingServiceBatchMixin.java"
sha256 = "be481f08623705791fb79700affd3940f9bb3f40a6f1aa43e9c278cd2a6845f1"

[[items]]
name = "push"
value = "Step 5: one operation per pattern, partial push"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCpuLogicBatchMixin.java"
sha256 = "c71ef2ab34d4aa565fc226554e06b6d8615b3f0ebea22c1baa5efb344355f8c2"

[[items]]
name = "buffer"
value = "Step 6: pattern buffer side"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferPatternBatchingMixin.java"
sha256 = "bd2057793549bddbfaeb5422a28ea8b9399425a43a3bb171ff54a5741a0d4a95"

[[items]]
name = "interface"
value = "Step 6: matrix side, pushPattern"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixInterfaceMachine.java"
sha256 = "d6b351d13c027a8e99620ac2cda52389c227e56c88cbcff3b1c31f8486f2af08"

[[items]]
name = "work"
value = "Step 7 and saved matrix state: tick, cancel, giveBackAll"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixWork.java"
sha256 = "61b05c8f54001e6527672d2c21591b98297fbbc9223a8a539f83b5f8602d366d"

[[items]]
name = "upload"
value = "Auto upload from the encoding terminal"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/PatternEncodingTermMenuUploadMixin.java"
sha256 = "2c6c6ff679eed25729c328f01b12d8218227a8a06f0fbf82d6bcad5fa2837514"

[[items]]
name = "batch"
value = "Saved batches: hidden Pattern Batch item and decoder"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/batch/PatternBatch.java"
sha256 = "b93a965e579b7fb562468e7b3c6da7b4de7454e358cd67f7ce108c133716e75b"
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
