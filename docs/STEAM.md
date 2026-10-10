# Advanced steam multiblocks and steam tweaks (`common/steam/`, `common/circuit/ControlCircuits`)

Split out of `context.md`. Toggles: `machines.advancedSteamMachines` (all of the first section as one unit) and
`steamTweaks.*`. Modelled on GTNL's steam multis (GTNH addon, LGPL-3.0, credited in README), not a copy. A
reference clone is at `E:\code\Minecraft\gtnl-ref`, outside the repo.

## Large steam multiblocks

- `AdvancedSteamMultiMachine extends SteamParallelMultiblockMachine` (the user's "advanced_steam_multi") has a
  `maxRecipeTier` per machine, default MV.
- Its `recipeModifier`:
  - rejects recipes above that tier;
  - applies non-perfect OC (×4 EU/t, ÷2 duration) from `max(tier, LV)` up to it, stopping at 1 tick. ULV gets one OC
    less, as GTCEu does;
  - then duration ×0.8 and EU/t ×0.75. Steam = EU × 2 mB, from the superclass.
- Parallels: 16, or the `SteamParallelHatchPartMachine` found in `formStructure`.
  - The hatch gives 16–256, default 256, with GTCEu's parallel hatch UI (÷2/×2).
  - It has its own `PartAbility` `steam_parallel_hatch`. Max one, not shareable.
- `AdvancedSteamMachines` registers `gtmqol:large_steam_<x>` for 21 recipe types.
  - The circuit assembler is MV, so it makes the ULV–HV control circuits. The magical assembler is LV, as the user
    asked.
  - All are bronze (the user: GTCEu steam multis have no steel tier), with the bronze theme, bronze plated bricks
    appearance and GTCEu's single-block overlay.
  - Hatch casing `X`: steam buses plus `autoAbilities` buses/hatches of any tier, one steam hatch and at most one
    parallel hatch. At least half of the `X` positions must be casings.
- `lp_/hp_steam_magical_assembler` (`registerSteamMachines` + `SimpleSteamMachine`, our magical assembler overlay).
  HP keeps GTCEu's steel theme, like other HP single blocks.
- Recipes:
  - LP steam magical assembler: shaped (bronze hull, crafting tables). HP: like GTCEu's steel upgrades.
  - Each multi, in the magical assembler: 1 LP steam single block + 2 bronze gearboxes + 4 bronze bricks + 8 bronze
    plates + 2 bronze gears. No circuits and ULV EU/t, so the steam magical assembler can make them.
  - Parallel hatch: a steam hatch + 2 bronze gearboxes + 4 LV circuits. The user wants no LV machines or circuits in
    the large steam multis themselves.
- The 14 bases GTCEu has no steam version of (bender … circuit assembler) get `gtmqol:lp_/hp_steam_<x>` via
  `GTMachineUtils.registerSimpleSteamMachines`, with GTCEu's overlay.
  - LP: `PCP/XMX/PXP` (bronze plates, small bronze pipes, bronze hull). `C` is a per-machine key item; for the
    circuit assembler it is 1 LV circuit, the only circuit, as the user asked.
  - HP: GTCEu's steel upgrade layout `WSW/PMP/WWW`.
- `AdvancedSteamShapes` was generated once by a Python script from GTNL's
  `assets/sciencenotleisure/multiblock/large_steam_*.mbs`.
  - The format is `MBS1`, then a string table of int length + UTF-8, then rows of int indices. The layout is
    `shape[row top→bottom][slice front→back]`, with strings along X, `~` for the controller and space for any block.
  - Rows are reversed to bottom-up for `MultiblockPatternBuilder.start()` (BACK, UP, RIGHT).
  - Every GTNL letter is remapped to one shared symbol set, documented on the class:
    - GT5 bronze casings → GTCEu bronze casings;
    - GTNL/GT++ decorative casings → bronze hull / bronze brick hull / plated bricks;
    - potin → bronze block;
    - glass → `forge:glass`.
  - The assembler and magical assembler have no GTNL large steam shape and use `steam_manufacturer`.
  - The shapes are possibly mirrored relative to GTNL (char direction). Harmless.
- `ControlCircuits`: GTNL's MetaItem 21–25 (very simple … elite, ULV–EV), with textures from GTNL, tagged
  `gtceu:circuits/<tier>`.
  - Recipes are ported from GTNL's `CircuitAssemblerRecipes` with GTCEu substitutes: cast iron → wrought iron; GT5
    circuits → NAND chip / electronic / integrated circuits, not consumed.
  - The names overlap Mekanism's basic/advanced/elite control circuits (display only, different ids).
- The whole project is LGPL-3.0 like GTNL/GTNH (`LICENSE`, text copied from GTNL, also packed into the jar; authors
  "Yiran, Frosty").

## Steam single blocks

- Full IO: `SimpleSteamMachineMixin` also attaches persistent fluid tanks sized by the recipe type (8 B, like LV).
  - The input tank refuses steam (`setFilter`).
  - It is attached with trait priority 2, above the steam tank, which is also an IN `NotifiableFluidTank`. GTCEu's
    machine UI uses the first IN fluid handler as the recipe slots, and `getTraits` is sorted by priority.
    Otherwise it drew the steam tank, and a 2-input mixer indexed past its one slot.
- `GTMachinesMixin` / `GTMultiMachinesMixin` drop every `addOutputLimit` in GTCEu's registration (steam macerator,
  LV–HV macerators, steam grinder, steam oven), because the user wants byproducts everywhere. Generator output
  limits (in `GTMachineUtils`) are untouched. Toggle: `steamTweaks.noOutputLimits`.
- `MagicalAssemblerUI` builds its 4-slot fluid column from the first IN fluid handler.
  - On a steam machine that used to be `SteamMachine.steamTank` (1 slot), so indexing slots 1–3 threw and the UI
    didn't open.
  - The column is now skipped when the handler is the steam tank. With the mixin's input tank first, the steam
    magical assembler shows and uses the fluid column; the steam tank check is only a fallback.
- Ghost circuits (`steamTweaks.circuitSlots`):
  - `SimpleSteamMachineMixin` attaches `ProgrammableCircuitSlotTrait` (as "circuit", like `SimpleTieredMachine`) to
    every steam single block, GTCEu's included.
  - `GTSingleblockMachinePanelsMixin` turns trait configurators back on for the steam panel, which
    `defaultSteamMachinePanelBuilder` disables, so the slot shows.
  - `SteamItemBusPartMachineMixin` re-enables the bus circuit slot GTCEu turns off (input bus only). That gives steam
    multis ghost circuits through their input bus.
  - The constructor inject must be `<init>*`: plain `<init>` only matched one of `SimpleSteamMachine`'s two
    constructors.

## Early game

- `GTMachineUtilsMixin` (`steamTweaks.optionalLargeBoilerParts`) makes the large boiler's muffler and maintenance
  hatch optional. It turns the two `setExactLimit` in the `registerLargeBoiler` pattern lambda into
  `setMaxGlobalLimited` (`require = 2`).
- `steam/EarlyGameRecipes`: ULV/LV machine casing + chest = bus, + glass = hatch. Chest or glass on top makes an
  input, below makes an output.
- `RecipeRemovalMixin` keeps `minecraft:glass` (sand smelting) out of gtceu's `hardGlassRecipes` removals.
- Magical assembler: any sapling + sticky resin → rubber sapling.
- Manual compression (3x3/2x2 block/ingot/nugget, raw ore block crafting, `recipes.keepManualCompression`):
  - Setting GTCEu's `ConfigHolder.INSTANCE.recipes.disableManualCompression = false` from our constructor did not
    work in game.
  - Now `ManualCompressionMixin` `@ModifyExpressionValue`s every read of that field (`GETFIELD`) to false. Targets:
    `RecipeAddition.disableManualCompression`, `RecipeRemoval.init`,
    `MaterialRecipeHandler.processNugget`/`processBlock`, `OreRecipeHandler.processRawOre`. That is one mixin with
    four targets, each with at least one match.
  - Recheck the list after a GTCEu bump (grep `disableManualCompression`).

## Known

- On steam (BRONZE theme) machine panels, including our wireless steam ones, the title and the GT logo are pushed
  to the right and look squeezed. GTCEu's own steam machines do the same, so it is upstream (2026-10-06).
- After a client resource reload, every bronze/steel themed UI fails to open; see `context.md` Pending.
