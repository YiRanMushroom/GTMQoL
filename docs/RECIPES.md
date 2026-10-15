# Recipes, magical assembler, circuits, tags (`common/assembler/`, `common/circuit/`, `data/`)

Split out of `context.md`.

## Where recipes come from

- Recipes go through `IGTAddon.addRecipes` (`GTMQoLAddon`). GTCEu runs it into its runtime data pack together with
  its own recipes, so no recipe JSON is datagen'd.
- Each feature has its own `addRecipes` (`GTMQoLMultiblocks`, `WirelessRecipes`, `MiscRecipes`, `EarlyGameRecipes`,
  `Gregification.addRecipes`, `AEProcessing`, ...), gated by the same config toggle as the feature.

## Magical assembler (`MagicalAssembler`, `MagicalAssemblerUI`)

- Recipe type `gtmqol:magical_assembler`: 16 in / 1 out items, 4 in / 1 out fluids, default `VA[LV]`. Machines via
  GTCEu's `SimpleMachineBuilder` (electric tiers), textures copied from `reference/`. It is always on: most of our
  recipes use it.
- Ported from the old `QoLRecipes.kt`:
  - the machine's crafting recipe (`PGP/GMG/PCP`);
  - circuit → universal circuit (circuit 5, 1 tick, 1 EU/t);
  - produce and copy the creative data access hatch.
- Not ported, because the outputs don't exist any more: the probable (im)probability devices and the industrial LCR.
- UI: the v8 recipe UI is ModularUI, configured through `GTRecipeType.UI(GTRecipeTypeUILayout.Builder)`.
  - Grids can be changed with `setLayoutGridBuilder` (`String[]`, `'s'` = slot). But every capability is stacked
    vertically in `inputColumn`, so putting fluids *beside* items needs custom per-capability builders
    (`setMachineCapabilityLayoutBuilder` / `setRecipeViewerLayoutCapabilityLayoutBuilder`).
  - The item builder puts a 4×4 item grid and a 1×4 fluid column in one row; the fluid builder skips IN. Outputs
    use the defaults.
  - On steam machines it skips the fluid column if the first IN fluid handler is the steam tank (`STEAM.md`).

## Circuits

- `UniversalCircuits`: `<tier>_universal_circuit` for every `GTValues.ALL_TIERS` tier, tagged `gtceu:circuits/<tier>`,
  with the old textures. The AE2 pattern encoder prefers them (`AE2.md`).
- `ControlCircuits` (ULV–EV, GTNL): see `STEAM.md`.

## Tags (datagen)

- `CircuitTags` (item tags):
  - `c:circuits/{basic,advanced,elite,ultimate}` (`forge:` on 1.20.1) includes `#gtceu:circuits/{lv,mv,hv,ev}`;
  - each GT tag includes the matching `mekanism:*_control_circuit` as an optional entry.
  - These are not mutual tag references, which would be a cycle. The old version rebound holder sets at
    `TagsUpdatedEvent`.
  - The tags are static JSON, so the `circuits.*` toggles don't remove them at runtime. This is a known limitation,
    left as is on purpose.
- `MaterialAliasTags` (item/block/fluid tags, always on): GT spells some materials differently from other mods.
  - Aliases: aluminium → aluminum, plutonium_239 → plutonium (`ALIASES`, also used by MI gregification).
  - For each `MaterialEntry` of the material (`ItemMaterialData.MATERIAL_ENTRY_ITEM_MAP` / `_BLOCK_MAP`) and each
    non-parent `TagPrefix` tag whose path contains the name, the alias tag includes `#<GT tag>` as an optional tag.
    Examples: `c:ingots/aluminum` → `#c:ingots/aluminium`; the `c:plutonium` fluid → `#c:plutonium_239`.
  - They are optional because GT's material tags are generated at runtime.
  - ULV pairs with infused alloy the same way:
    - `c:alloys/advanced` includes `#gtceu:circuits/ulv`. Mekanism recipes use both `c:alloys/advanced` and
      `mekanism:alloys/infused`, and the latter includes the former.
    - `gtceu:circuits/ulv` includes `mekanism:alloy_infused`.

## Wireless recipes (`WirelessRecipes`, magical assembler, LV)

- GT energy input/output hatch + circuit 5 → wireless hatch.
- Wireless hatch + circuit 5 → 4 covers of the same direction.
- GT output hatch + 4 input covers → accessor.
- Screen cover + LV input cover → monitor.
- Steam works the same way, from GT's `STEAM_HATCH`. The output hatch uses circuit 6, since GT has no steam output
  hatch.

## Legacy part recipes (`common/part/LegacyPartRecipes`, `recipes.useLegacyPartRecipes`)

GTCEu v8 (both branches, from the 2026-10-13/14 snapshots) removed the rod, bolt, gear, small gear and rotor extruder
molds: rods come from the lathe (25 mB lubricant doubles the output, `LatheRecipeLogic`), gears from the new forming
press recipes, rotors from the welder (4 plates + a ring). The config (default on) brings them back as
`gtmqol:<shape>_extruder_mold`, with the extruder recipes copied from v7/early v8 `MaterialRecipeHandler` /
`PartsRecipeHandler`, plus a long rod mold (1 ingot → 1 long rod, same time and power as 2 rods; GTCEu only had its
texture). Item models point at gtceu's textures, which are still shipped; models and lang need `runData`.

- Mold crafting, file + mold like GTCEu's: v8 gave every file position around the empty mold to other molds (the old rod
  and rotor patterns are pipe molds now), so rod = wire mold + file right, long rod = rod mold + file below, bolt =
  rod mold + file top left, gear / small gear / rotor = ring mold + file top left / top / bottom left. Copying in the
  forming press like GTCEu's molds.
- Rotors: `TagPrefix.rotor.materialAmount` goes back to `M * 4` (v8: `M * 17 / 4`, for the plates + ring), so 4
  extruded ingots don't recycle into 4.25. It applies to every rotor, welded ones too.
- Recipe ids are ours (`gtmqol:extrude_<material>_...`), so they don't clash with anything GTCEu might add back.

## Misc

- Mekanism is compiled against on both branches (chemicals, gregification), only touched when it is loaded.
- Other recipe tweaks (vanilla TNT, nether star dust, manual compression, early game) are in `MULTIBLOCKS.md` and
  `STEAM.md`.
