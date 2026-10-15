# Upgrading GTCEu v8 to the latest snapshot (survey of 2026-10-14)

**Applied on 2026-10-14 on both branches** (versions, "Must change", the two deleted 1.21 workarounds, the two
workarounds added to 1.20.1). Both compile against the new versions; the in-game checks below are still open. Kept
as the record of what changed upstream. Found by diffing the GTCEu sources jars and reading our mixin targets
against the new code.

| | was pinned | now pinned |
|---|---|---|
| 1.21.1 | `8.0.0-20261001.095212-101` (after `d2ef26f`) | `8.0.0-20261014.192230-122` (`44ea3da`), ~36 commits |
| 1.20.1 | `8.0.0-20260926.222216-292` (`246560d`) | `8.0.0-20261013.044024-319` (`b4bf41b`), 37 commits |

Upstream's `forge.versions.toml` / `libs.versions.toml` didn't change (registrate, configuration, AE2, EMI, JEI,
NeoForge 21.1.248, `mui = 3.3.1-SNAPSHOT`). But the MUI snapshot itself moved, so bump `modularui_version` with
GTCEu: 1.21 `3.3.1-20260922.143646-5` → `3.3.1-20261013.074510-7`, 1.20.1 `3.3.1-20260913.083747-19` →
`3.3.1-20261013.074125-23` (latest when surveyed; recheck).

## Must change (both branches unless noted)

- `GTMachinesMixin`: `require = 2` → `1`. Upstream #5528 (Rebalance Macerator) removed the LV–HV macerator
  `addOutputLimit`; only the steam macerator's is left, so the mixin would fail to apply (crash at load). Update the
  javadoc ("the only two calls"). The LV–HV half of `steamTweaks.noOutputLimits` is now upstream behaviour.
- 1.21 `StructurePlanner` (auto-build): calls `MultiPredicate.getParent()`, removed by the predicate rework
  (#5342/#5407: `getParent/setParent/isRoot/isBranch/isLeaf` gone). Probably the root walk can just go, since the
  helper already hands out the root (unverified). 1.20.1 didn't get that rework.
- 1.21 `VoidMinerOres` (`ChemicalHelper.getItem(...)` then `item == Items.AIR`): `getItem` now returns `null` instead
  of AIR; check `item == null || item == Items.AIR`.
- `AEDualParts` javadoc says LuV "like the other ME buses and the pattern buffer": upstream lowered them (#5518/#5519:
  ME buses/hatches HV, stocking EV, pattern buffer IV, proxy LuV). Our dual/universal parts stay LuV unless we
  decide otherwise.

## Check in game after upgrading

- 1.21 auto-build block choices: `Predicates.blocks/heatingCoils/frames/...` now build `MultiPredicate.or` of one
  `BasePredicate` per block instead of one predicate with N candidates. `BuildOptions.choices` indices and the number
  of options per symbol change, so saved `BuildSettings` may point at different blocks. `MultiPredicate` got
  value-based `equals/hashCode` (we key autobuild maps by identity, so fine; the helper's tables now use value
  equality). Limit setters (`setExactLimit`, `setMinGlobalLimited`, ...) now return copies; our call sites chain them.
- `ExpandablePattern` now resolves predicates through a `char` symbol map (#5488). `getPredicateFromPos` /
  `getBoundsConstraints` keep their signatures; auto-build should be unaffected (unverified at compile time).
- 1.20.1 `GTTransferUtils` got a large rewrite (transfer limiters). Our `getAdjacentFluidHandler` /
  `transferFluidsFiltered` calls (`WirelessSteamAccessorMachine`) looked unchanged, but only part of the diff was read.

## Workarounds upstream already fixed (delete after upgrading, 1.21)

- `gtceufix/GTMuiWidgetsMixin`: #5540, `GTMuiWidgets` now does `stack(v, Math.max(1, ...getCount()))`. Still applies,
  just redundant.
- `gtceufix/GTRecipeTypeMixin`: #5541, the `GTRecipeType` constructor now copies `customRecipeLogicRunners` from the
  properties. Our `isEmpty()` guard makes it a no-op, so no double add in the meantime.

## Workarounds still needed

- 1.21, unchanged upstream (target classes byte-identical): `GTRecipeBuilderMixin`, `GTEmiRecipeMixin`,
  `FluidRecipeCapabilityMixin`, `GTRecipeCategoryMixin`, `EnergyContainerListMixin`, `RecipeRunnerMixin`,
  `RecipeLogicMixin`, `EmiCallWrapperMixin`, `MEPatternBufferInternalSlotMixin`, `AEConfigSyncHandlerMixin`,
  `PotionFluidTypeMixin`, `GameDataMixin` (NeoForge target, premise unchanged). The new `LatheRecipeLogic` overrides
  `findAndHandleRecipe`/`onRecipeFinish` on the unchanged base, so `RecipeLogicMixin` isn't affected.
- 1.20.1: `EnergyContainerListMixin` still needed (`hasPrimeFactorGreaterThanTwo` still divides up to `l / 2`, and the
  multiblock energy line still rebuilds the list every tick).
- ModularUI/JEI targets (`AbstractWidgetMenuMixin`, `BaseSchemaRendererMixin`, `jei/JeiRecipeSlotMixin`,
  `ItemDataComponentMapIngredientMixin`) weren't checked; recheck them against the new MUI snapshot.

## 1.20.1 is missing two workarounds

`PORT_1_21.md` says `RecipeRunnerMixin` and `RecipeLogicMixin` are on both branches; they aren't, and 1.20.1 has both
bugs (in the pinned and the latest build):

- `ChanceLogic.java:54` (OR) and `:216` (XOR): `int totalChance = times * newChance;`, with `times` from
  `recipe.getTotalRuns()` (`RecipeRunner.java:110`). Overflows above ~214k runs.
- `RecipeLogic.java:388-398`: restarting the last recipe sets `lastOriginRecipe = null` and nothing sets it back, so
  the next `onRecipeFinish` (`alwaysTryModifyRecipe`) marks the recipe dirty and goes idle.

`RecipeLogicMixin` should port as is. `RecipeRunnerMixin`'s merge step uses NeoForge's `SizedIngredient` /
`SizedFluidIngredient` and needs a Forge rewrite.

## Checked, no change needed

- Mixins on changed classes: `GTMultiMachinesMixin` (both `addOutputLimit` calls and `getBlockRange` still there),
  `GTMachineUtilsMixin` (`registerLargeBoiler` still has the two `setExactLimit`), `ManualCompressionMixin` (same
  `disableManualCompression` reads), 1.20.1 `WorkableElectricMultiblockMachineMixin`, `SteamItemBusPartMachineMixin`.
- API changes we don't use: `NotifiableFluidTank` lost its locking members (moved to the new `FilteredFluidTank` /
  `LockableFluidTank`), `ConfigHolder.recipes.harderRods`, `GTItems.SHAPE_EXTRUDER_*`, `TagPrefix.itemTable` (renamed
  `customLookupTable`), `Material.multiply`, deprecated `Material.getHotFluid` and the molten fluid key (merged into
  liquid, #5531), `RegistryReferenceTransformer`. `ProgrammableCircuitSlotTrait.setCircuit(<=0)` now clears;
  `IntCircuitBehaviour.stack` no longer throws. `IRecipeLogicMachine` gained default `isWaiting/isSuspend`.
  `SimpleMachineBuilder` gained a `recipeLogic` supplier (our `MagicalAssembler` only calls the constructor).
  `ItemBusPartMachine` / `SteamParallelMultiblockMachine` changes are UI only.

## Overlaps with new upstream features

- ME part recipes (#5518, both): upstream now makes the ME buses/hatches/stocking parts/pattern buffer in the
  assembler at lower tiers. Our `AEProcessing` ME assembler recipes for the same items become a second path;
  decide whether to keep them.
- Lathe rework (1.21): stone rod extra output now comes from `LatheRecipeLogic` (electric lathes only), so our large
  steam lathe gives one rod.
- Rotor material amount is now `M * 17 / 4`; we only use the rotor as an item.
- Wireless Chargers (#5501), Ore Fortune config (#5552), welder/spooler machines, GCYM multis: no overlap.
