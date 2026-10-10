# Modular machines (`common/modular/`)

Split out of `context.md`. Config `modularMachines.enabled` (default true).

## Which machines

- `MachineBuilderMixin` (RETURN of `MachineBuilder.register()`) hands every non-multiblock machine to
  `ModularMachines`. `registerTieredMachines`, KubeJS tiered machines and addons looping their tiers themselves all
  end in `register()`.
  - This replaced the earlier approach on 1.20.1: RETURN of `registerTieredMachines` plus a
    `KJSTieredMachineBuilder` mixin. That missed self-looping machines.
- The user's rule (2026-10-08): for each set of recipe types, the first machine registered with tier ≥ LV gets the
  modular machine. Steam machines share the types but are tier 0, so the LV one wins.
  - Only machines named `VN[tier].toLowerCase()+"_"<name>` count. GTCEu (and addons copying it) register `hp_` steam
    machines with `tier(1)` = LV, and before this check they were picked as "the LV machine" for their recipe types.
  - The modular machine is named after that machine with the prefix stripped. If the name is taken by a different
    recipe type set, a warning is logged and it gets none.
- On 1.21, `MachineBuilderMixin` only queues: the builder name, `properties().tier()`, and the unresolved recipe type
  suppliers (through `MachineBuilderAccessor`). They are deduped in `declare` once the suppliers resolve.
  - The multiblocks are declared at the start of the `gtceu:machine` registry event.
  - Machines registered after that log "registered after the modular machines were declared" and get none. KubeJS
    creates its objects in the registry event, possibly later. Unverified (2026-10-08).
- `ModularMachines.declare` registers `gtmqol:modular_<name>` (other addons: `modular_<ns>_<name>`) through our
  registrate with `dynamicallyGenerated(true)`. Models and en_us lang are made at runtime, nothing is datagen'd,
  and it is skipped during datagen.
- Skipped, as in the old `AddModularMultiblocksLogic.kt`: machines without recipe types, with `DUMMY_RECIPES`, or
  mixing generator and non-generator types.

## The multiblock

- 3×3×3, controller at the front centre, any block allowed (casing/glass preview, auto abilities). No parallel
  hatch: the user only wants subtick parallels.
- Modifiers: duration ×0.125 (generators ×8), then `OC_PERFECT_SUBTICK` (generators `create(0.5, 4.0, true)`), then
  `BATCH_MODE`. If every recipe type is gregified (`Gregification.allGregified`), the machine uses
  `GregificationModifiers` instead.
- Overlay: the single block's `block/{machines,generators}/<name>` if it has `overlay_front.png`, else the implosion
  compressor / large combustion engine.
- Recipes: magical assembler (circuit 5) and a hammer-shaped recipe, from the first tier.
- `ModularMachine.getMaxVoltage()` returns `getOverclockVoltage()` (the old
  `SingleHatchTierSkippingWorkableElectricMachine`).
- Not ported: the `QOL_RECIPE_MODIFIER` part ability, non-English names.
