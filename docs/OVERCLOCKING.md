# Overclocking (`common/overclock/`, `OverclockingLogicMixin`)

Split out of `context.md`. Gregified machines use their own logic instead (`GREGIFICATION.md`).

## Overhaul (`overclocking.overhaul` in `gtmqol-early.properties`)

The factor constants (`STD_VOLTAGE_FACTOR` …) are interface fields. That makes them implicitly `static final`
compile-time constants, inlined by javac, so patching them does nothing. The four logic constants are objects, but
`static final` too; the old `OverclockingPatcher` replaced them with Unsafe.

Instead, `OverclockingLogicMixin` (interface mixin, `@Overwrite` only, the old project's approach) overwrites:

- `getModifier`:
  - `Overclocking.replace` maps `NON_PERFECT_OVERCLOCK(_SUBTICK)` → 4× EU/t for 4× speed, and
    `PERFECT_OVERCLOCK(_SUBTICK)` → 2× EU/t for 4× speed, both subtick.
  - It always computes parallels (`getParallelAmountWithoutEU`).
  - No ULV OC penalty, as in the old mixin.
- `subTickParallelOC`: the equivalent OC, with fractional OC levels and the speed rounded down to whole parallels
  once at 1 tick. It affects every subtick logic, including generators' `create(0.5, 4.0, true)`.
- `heatingCoilOC`: every OC is 4× EU/t for 8× speed, and coil temperature is ignored (the old behaviour).

Two more mixins come with it:

- `GTRecipeViewerWidgetMixin` applies the same replacement to the recipe viewer's OC preview, which calls
  `runOverclockingLogic` directly.
- `GTRecipeModifiersMixin`: with the new OC computing parallels, the multi smelter's base → OC → parallel order gives
  wrong results. So the middle OC of `multiSmelterParallel` becomes identity, and the OC is appended to the returned
  function, computed on the parallelized recipe.

All three are switched together by the early option. Overwrites can't be switched off at runtime, so
`GTMQoLMixinPlugin` decides whether they apply at all.

## Toggles under `overclocking.*` in `gtmqol.yaml` (default true, restart)

- `buffFusionReactor`:
  - `FusionReactorMachineMixin` wraps `FUSION_OC = create(...)` in `<clinit>` to return `PERFECT_OVERCLOCK_SUBTICK`.
  - It also wraps the `getModifier` calls in `recipeModifier` to pass `getOverclockVoltage()` with parallels, instead
    of the tier-capped `getMaxVoltage()`.
  - `GTMultiMachinesMixin` adds substation and laser hatches to the fusion 'E' slot. It wraps
    `PartAbility.getBlockRange(II)` (its only call in `GTMultiMachines`) inside a lambda selected by regex
    (`/^lambda\$/`, works).
- `enableMultiTierSkipping`: `WorkableElectricMultiblockMachineMixin` wraps `return V[...]` in `getMaxVoltage` (the
  "several hatches at the highest tier → tier + 1" branch). It returns the summed hatch voltage instead (`VEX[floor
  tier]` when amperage is 1).
