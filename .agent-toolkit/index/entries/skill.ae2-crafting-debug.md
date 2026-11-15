+++
title = "Debugging AE crafting, batches and the Assembling Matrix: a job's path, saved state, symptoms; use when a job misbehaves"
kind = "howto"
keys = [ "assembling matrix bug", "batch debugging", "crafting job stuck", "lost items", "waitingfor" ]
updated = "2026-11-15 22:55:55"
checked = "2026-11-15"

[[items]]
name = "plan"
value = "Step 1: our planner before AE2's tree"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCalculationPlannerMixin.java"
sha256 = "dd308c0fad22f1534e3a7693f3078bebb5ac612e1342ab7fddbc1a9f97940d74"

[[items]]
name = "cut"
value = "Step 2: batches cut from the plan"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingSimulationStateBatchMixin.java"
sha256 = "63fb04e2c30e4ee4bd1decdcb41116857bbf5420f0c0c102e44560e0199ea66b"

[[items]]
name = "providers"
value = "Step 4: getProviders unwraps batches"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingServiceMixin.java"
snippet = "private IPatternDetails gtmqol$patternOfBatch(IPatternDetails key)"

[[items]]
name = "push"
value = "Step 5: one operation per pattern, partial push"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCpuLogicBatchMixin.java"
sha256 = "d0456346e6686913941bde4042589b743ba008f1138502fe09dd9c363bf84e78"

[[items]]
name = "buffer"
value = "Step 6: pattern buffer side"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferPatternBatchingMixin.java"
sha256 = "54c277f8d7508aecf1d337b64943b655c1254e344141154b7db3267f40f67a95"

[[items]]
name = "interface"
value = "Step 6: matrix side, pushPattern"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixInterfaceMachine.java"
sha256 = "30c74ee969714e760ca9a5876ef2abc33e651a6220b16b87e8cd6646c164c2f1"

[[items]]
name = "work"
value = "Step 7 and saved matrix state: tick, cancel, giveBackAll"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixWork.java"
sha256 = "b715da3044ddc65396de71096ea9c424dcecf17a38422cd7c23982e588de198b"

[[items]]
name = "upload"
value = "Auto upload from the encoding terminal"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/PatternEncodingTermMenuUploadMixin.java"
sha256 = "dbbcea811a918633c0bd3d05a1b2e86ea554008ad6f97de8f69db278aa3d8cf0"

[[items]]
name = "batch"
value = "Saved batches: hidden Pattern Batch item and decoder"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/crafting/PatternBatch.java"
sha256 = "967ae70f59037586f010a3dbeb9db2548264c82fdcd0bd28c108b7b0f6d46eb6"
+++
For when something goes wrong in game. Why things are as they are: ae2.fast-crafting (planner, batches, push time), ae2.pattern-matrix, ae2.pattern-batching. Class names below are in integration/ae2/crafting, integration/ae2/matrix and core/mixins/ae2.

## A job's path
1. **Plan** (CraftingCalculationPlannerMixin, HEAD of runCraftAttempt, config ae2.fastCraftingPlans): CraftingSolver over AECraftingPlanner. PLANNED: AE2's buildCraftingPlan builds the plan from our simulated inventory. MISSING (also after MAX_WALKS = 300 walks): the preview shows what is missing; a real order gets no plan. REFUSED: AE2's own tree runs as if we weren't there (give-backs, loops that matter, substitutes when short).
2. **Cut** (CraftingSimulationStateBatchMixin, HEAD of buildCraftingPlan -> BatchPlan.cut): a pattern whose providers are all PatternBatchProvider becomes one PatternBatch of all its crafts, or as few as the limit allows (BatchSizes.pieces). Limit = smallest batchLimit of its providers.
3. **Submit** (AE2): the CPU takes all stock the plan uses into itself (tryExtractInitialItems); no other job can take it after.
4. **Providers** (CraftingServiceBatchMixin): getProviders unwraps a PatternBatch, so the original's providers are asked.
5. **Push** (AE2's CraftingCpuLogic.executeCrafting + CraftingCpuLogicBatchMixin, AE2's CPU only): a pattern's batches in one pass cost one operation; a last piece of its size that can't go whole pushes the part inputs and power allow (PartialPush), the rest goes back as its own task after the loop. A pattern that can't get one craft's inputs is skipped for the rest of that pass (its bigger pieces can't either). AE2 adds the pushed pattern's outputs to waitingFor; for a part they are the part's.
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
- **Batch waits (chain runs step after step)**: a CPU that isn't AE2's pushes a batch only whole, so it waits for all its inputs, and forever if it needs more AE power than the network stores; on AE2's CPU a repeated size (x2 limit) that the partial push skips.
- **Too many CPU operations**: the one-operation rule is only on AE2's CPU.
- **Pattern in the matrix but not offered**: only AE2 crafting/stonecutting/smithing (IMolecularAssemblerSupportedPattern) and our crafting patterns; equal patterns count once; needs a formed matrix with its interface.
- **Auto upload doesn't take it**: not Shift-clicked (only Shift-click Encode uploads; the client's Request must reach the server before AE2's encode action), switched off, no free slot or the same pattern already there (both leave it in the terminal with a toast), processing patterns never; PatternEncodingTermMenuUploadMixin runs before ExtendedAE Plus (priority 500).
- **Wrong amounts back on cancel**: per-slot accounting in Queued/Slot; crafts from saves before it give nothing back.
- **Crash on world load in a decoder**: a null key (GT's buffer decodes empty slots); decoders check for null.
- **EAP installed**: its CraftingCpuLogicPatternPowerMixin also hooks executeCrafting (only its own pattern batches).

## Checking without the game
Unit tests (gradlew test, integration/ae2/crafting): TestWorld plays plans out as a CPU would; PartialPush against trying every count. Mixins: a temporary Class.forName(name, false, loader) in PatternBatch.init, then runData, then remove it.
