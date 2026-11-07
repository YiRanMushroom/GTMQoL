+++
title = "Troubleshooting AE crafting, batches and the Pattern Matrix: a job's path, saved state, symptoms"
kind = "howto"
keys = [ "batch debugging", "crafting job stuck", "lost items", "pattern matrix bug", "waitingfor" ]
updated = "2026-11-07 10:49:37"
checked = "2026-11-07"

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
sha256 = "28e75b2aea60541007b820bbfeefce48cda155a4d74fb6f0c4cbd6ab5045c217"

[[items]]
name = "push"
value = "Step 5: one operation per pattern, partial push"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCpuLogicBatchMixin.java"
sha256 = "2e0232b0e5852054b7252a502f38604fcef7ecfdc3f6a99f3439b45f56dad7bb"

[[items]]
name = "buffer"
value = "Step 6: pattern buffer side"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferSmartDoublingMixin.java"
sha256 = "9905bb45f4992bb88fc322df890af2a3f3ef2bdf2ee298b3d16945141a222b24"

[[items]]
name = "interface"
value = "Step 6: matrix side, pushPattern"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/MatrixInterfaceMachine.java"
sha256 = "8ae7cd4448b0b4263b3931d49e6572cd6792e4646b6b58c5b7de65655a50fb54"

[[items]]
name = "work"
value = "Step 7 and saved matrix state: tick, cancel, giveBackAll"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/PatternMatrixWork.java"
sha256 = "25dd6891a53b5c031dde45c230e6dcd378e2cfd4f61effe9d0519f671b83a0c0"

[[items]]
name = "upload"
value = "Auto upload from the encoding terminal"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/PatternEncodingTermMenuUploadMixin.java"
sha256 = "258e9d331372dc9889d4070dcb46d05528508486658dff698f3f46d85cf2b718"

[[items]]
name = "scaled"
value = "Saved batches: hidden item and decoder"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/batch/ScaledPattern.java"
sha256 = "a2db87750c1c05e13a8c03321739c5f3ea2a13c967a1d061015eb5b21dd05dd3"
+++
For when something goes wrong in game. Why things are as they are: ae2.fast-crafting (planner, batches, push time), ae2.pattern-matrix, ae2.smart-doubling. Class names below are in integration/ae2/batch, integration/ae2/matrix and core/mixins/ae2.

## A job's path
1. **Plan** (CraftingCalculationPlannerMixin, HEAD of runCraftAttempt, config ae2.fastCraftingPlans): CraftingSolver over AECraftingPlanner. PLANNED: AE2's buildCraftingPlan builds the plan from our simulated inventory. MISSING (also after MAX_WALKS = 300 walks): the preview shows what is missing; a real order gets no plan. REFUSED: AE2's own tree runs as if we weren't there (give-backs, loops that matter, substitutes when short).
2. **Cut** (CraftingSimulationStateBatchMixin, HEAD of buildCraftingPlan -> BatchPlan.cut): a pattern whose providers are all BatchCraftingProvider becomes ScaledPattern batches: whole when its inputs come from stock and AE power is there, else BatchPieces.split (1, 2, 4.. and the rest; ae2.batchParts). Limit = smallest batchLimit of its providers.
3. **Submit** (AE2): the CPU takes all stock the plan uses into itself (tryExtractInitialItems); no other job can take it after.
4. **Providers** (CraftingServiceBatchMixin): getProviders unwraps a ScaledPattern, so the original's providers are asked.
5. **Push** (AE2's CraftingCpuLogic.executeCrafting + CraftingCpuLogicBatchMixin, AE2's CPU only): a pattern's batches in one pass cost one operation; a last piece of its size that can't go whole pushes the part inputs and power allow (PartialPush), the rest goes back as its own task after the loop. AE2 adds the pushed pattern's outputs to waitingFor; for a part they are the part's.
6. **Provider side**: pattern buffer (MEPatternBufferSmartDoublingMixin, unwraps, pushes the batch's inputs) or MatrixInterfaceMachine.pushPattern (unwraps, PatternMatrixMachine.push: catalysts first, then PatternMatrixWork.add).
7. **Matrix work, every tick** (PatternMatrixWork.tick): first giving-back runs (all or nothing), then each queued pattern once, up to capacity, paid in FE (patternMatrixFEPerCraft); products into `outputs`, flushed to the network through the interface each tick. The CPU takes what it waits for first (AE2 gives a CPU's insert top priority).

## What is saved
- CPU tasks: by pattern definition. A batch is the hidden item scaled_crafting_pattern (original + multiplier), decoded back by ScaledPattern's decoder, so restarts keep sizes. EAP's scaled patterns lose theirs (not ours).
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
- **Batch waits forever**: needs more AE power than stored on a CPU that isn't AE2's, or a repeated size (x2 limit, batchParts > 1) that the partial push skips.
- **Too many CPU operations**: the one-operation rule is only on AE2's CPU.
- **Pattern in the matrix but not offered**: only AE2 crafting/stonecutting/smithing (IMolecularAssemblerSupportedPattern) and our crafting patterns; equal patterns count once; needs a formed matrix with its interface.
- **Auto upload doesn't take it**: switched off, no free slot, the same pattern already there (then the copy becomes a blank), processing patterns never; PatternEncodingTermMenuUploadMixin runs before ExtendedAE Plus (priority 500).
- **Wrong amounts back on cancel**: per-slot accounting in Queued/Slot; crafts from saves before it give nothing back.
- **Crash on world load in a decoder**: a null key (GT's buffer decodes empty slots); decoders check for null.
- **EAP installed**: its CraftingCpuLogicPatternPowerMixin also hooks executeCrafting (only its own scaled patterns).

## Checking without the game
Unit tests (gradlew test, integration/ae2/batch): TestWorld plays plans out as a CPU would; PartialPush against trying every count. Mixins: a temporary Class.forName(name, false, loader) in ScaledPattern.init, then runData, then remove it.