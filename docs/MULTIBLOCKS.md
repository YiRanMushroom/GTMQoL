# Multiblocks (`common/multiblock/`, `common/crystal/`, `common/greenhouse/`, `common/implosion/`)

Split out of `context.md`. Each machine has a toggle under `machines.*`. `GTMQoLMultiblocks` has one init/recipe
method per machine. Gregified multiblocks are in `GREGIFICATION.md`, modular machines in `MODULAR_MACHINES.md`,
steam multiblocks in `STEAM.md`.

## Smart Assembly Factory and DTFR

These are ports of the old ones. The user  means the Smart Assembly
Factory. Lang and models are datagen'd; their recipes are in the magical assembler.

- Smart Assembly Factory: assembly line recipes, with `PARALLEL_HATCH`, `OC_PERFECT_SUBTICK` and `BATCH_MODE`.
  - The pattern goes from 7.x `start(BACK, UP, RIGHT)` to v8 `start(RIGHT, UP, BACK)`.
  - `setRepeatable(4)` becomes `sliceRepeatable(4, 4, ...)`.
  - The data hatch comes through `.and(dataHatchPredicate())`, which is null when research is off.
- DTFR: fusion recipes of any tier (ignores `eu_to_start`), same modifiers.
  - `DTFRMachine` is a plain `WorkableElectricMultiblockMachine`, with no fusion buffer, heat or tier cap.
  - It holds the ring fade state itself, because the dynamic render instance is shared.
  - The layout is fusion MK3, with all fluid slots as 'X'. Energy slots also accept substation and laser hatches
    (new vs 7.x).
  - The ring is `client/DTFRRingRender`: gtceu's `FusionRingRender` without bloom, in white. It is registered as
    `gtmqol:dtfr_ring` in `GTMQoLClient.init()`.

## Nether stars, electric implosion compressor (`implosion/ElectricImplosion`)

Goal: nether stars before IV (IV needs a lot of them).

- `recipes.keepVanillaTNT`: `RecipeRemovalMixin` `@ModifyExpressionValue`s the `removeVanillaTNTRecipe` read in
  `RecipeRemoval.generalRemovals` to false. This keeps `minecraft:tnt` crafting regardless of GTCEu's config.
- `recipes.netherStarDust`: a mixer recipe, 4 diamond dust + 16 silver dust → 1 nether star dust, 20 s, `VA[HV]`
  (`MiscRecipes.addNetherStarDust`, the user's choice). GTCEu has no nether star dust synthesis, and GTExpert's
  recipe needs rocket fuel at LuV. The dust then goes through GTCEu's normal implosion recipes.
- `machines.electricImplosionCompressor` is a port of the 1.19 one.
  - Recipe type and multiblock `gtmqol:electric_implosion_compressor`: the old 1.19 pattern, robust tungstensteel
    casing, parallel hatch, perfect subtick OC, batch.
  - Recipes: GTCEu makes four implosion variants per material (powderbarrel, TNT, dynamite, ITNT). Only the vanilla
    TNT one (`implode_<x>_tnt`) is copied, as `implode_<x>_electric`, without the TNT and with 4× duration.
  - No mixins (1.19 used two). At common setup an `onSave` is chained onto `IMPLOSION_RECIPES`' prototype builder,
    which `recipeBuilder(id)` copies. Recipes are built later, on server reload.
  - Any earlier `onSave` is kept and called; implosion normally has none. Someone setting it after us without
    chaining would drop ours.
  - GTCEu's `RecipeManagerLateMixin` rebuilds every loaded `GTRecipe` with its type's prototype `onSave` at the end
    of `RecipeManager.apply`. So data pack and KubeJS implosion recipes get copies too; same ids overwrite, so there
    are no duplicates.
  - Controller: shaped `PCP/FSF/PCP` from ZPM circuits, an implosion compressor, IV motors and field generators, as
    in 1.19.
  - Not yet in game. On 1.20.1, KubeJS implosion recipes get no copy, because there's no late regeneration.

## Cyclic multiblocks (`CyclicMultiblockMachine`, `PendingOutputTrait`)

An abstraction the user asked for, shared by the void miner and the fishing pond.

- `CyclicMultiblockMachine extends MultiblockControllerMachine implements IMuiMachine` has its own server tick and no
  recipe logic, because GTCEu's voids overflowing outputs.
  - States:
    - IDLE → WORKING: lasts `cycleTicks()`, calls `onCycleTick()` every tick, pauses while unformed.
    - WORKING → `finishCycle()`, which adds to `output`.
    - OUTPUTTING: retries every `RETRY_TICKS` = 5.
    - Back to IDLE.
  - While enabled in IDLE, `startCycle()` is retried every 5 ticks. It pays, and returns null or a problem lang key.
  - Stopping lets the current cycle finish.
  - `RECIPE_LOGIC_STATUS` is set by hand (WORKING only while working).
- Shared UI pieces: status line, stored button + live popup (fixed rows, `setEnabledIf`), power toggle, popup,
  counter row (Shift step per machine). Lang keys are under the machine's prefix; common ones go through
  `GTMQoLMultiblocks.addCyclicMachineLang`.
- `PendingOutputTrait` has parallel `@SaveField` lists:
  - `items`: count-1 templates, merged by `isSameItemSameComponents`, so enchanted books and damaged rods survive;
  - `counts`: longs.
  - `output()` inserts into every `NotifiableItemStackHandler` with `IO.OUT`, so the ME output bus works. Breaking
    the controller loses what is pending.
- The refactor changed the void miner's saved fields: the `pending` CompoundTag is gone, and state `MINING` became
  `WORKING`. Old placed miners lose their pending ores and may load in an odd state.

## Void miner (`VoidMinerMachine`, `VoidMinerOres`)

Both branches.

- No energy hatch (`DUMMY_RECIPES`). The shape is GTCEu's EV Large Miner with solid steel casing and steel frames.
  'X' only takes output buses (≥1). The model copies the large miner's (active parent when formed).
- Power: `WirelessBindingTrait` (auto-binds on placement, data stick works) and
  `WirelessEnergySavedData.extract(network, 1, cost)`, all-or-nothing.
  - 1M EU per stack (64 ores).
  - Operations 1..16 × stacks 1..16, paid in `startCycle()`.
- Semantics (corrected by the user): one stack is 64 of the *same* ore.
  - Each operation draws one ore (binary search over the cumulative chances) and yields `multiplier` stacks of it.
    So operations = the max distinct ores per cycle.
  - Settings are snapshotted into `cycleOperations`/`cycleMultiplier` at payment; changes apply next cycle.
- A cycle is 300 ticks. The next cycle only starts once everything is out, so at most 16 ore types are pending.
- Ores:
  - Source: GTCEu's `ORE_VEIN` datapack registry. The user's "GTNH veins" means GT's own veins.
  - Veins whose `dimensionFilter` has the dimension count. Each adds `weight × chance / Σchances` per material.
  - Prefix: overworld deepslate, nether netherrack, end endstone. Otherwise the `TagPrefix.ORES` entry whose stone
    is the dimension's noise `defaultBlock`, else deepslate.
  - `voidMiner.dimensionMapping` (`"from=to"`) mines another dimension's veins and stone.
  - Computed once per machine load; a datapack `/reload` needs a chunk reload.
- UI: wireless binding block (`WirelessEnergyUI.create`), status and settings lines, and buttons for the ore chances
  popup, stored popup, settings popup (±1, Shift ±4) and power. Popups are `syncedPanel`s.
- Recipe (magical assembler, LV, no chips): 4 LV miners, 16 LV circuits, 16 each of LV motor/piston/conveyor,
  16 solid steel casings, 64 double steel plates, 16 steel gears.

## Industrial Fishing Pond (`FishingPondMachine`, `client/FishingPondWaterRender`)

Config `machines.fishingPond`. Forms in game on 1.21.1. On `CyclicMultiblockMachine`.

- Structure: GTCEu's Large Chemical Bath widened.
  - 7 × 7 × 5 watertight casing around a 5 × 5 × 4 air cavity. That matches vanilla's open-water check (5 × 5,
    y−1..y+2). The top is open.
  - The controller is in the front wall, at the cavity's bottom layer.
  - 'X': ≥100 casings, ≥1 output bus, ≥1 `INPUT_ENERGY` hatch (only that ability, the user's call). Chem bath model.
- The pattern needs an explicit `startOffset(OriginOffset.of(BACK, 6).move(DOWN, 1).move(LEFT, 3))`.
  - GTCEu's automatic offset counts a repeatable slice once. With `sliceRepeatable(5, 5, ...)` before the
    controller's slice it checks the wrong blocks and never forms (GTCEu bug).
  - `startOffset` is the vector from the controller to the first char of the first slice.
- Water: a persistent `MultiblockFluidRendererTrait` plus our own `DynamicRender` (always water, same
  `FluidBlockRenderer` settings as GTCEu's chem bath).
  - The offsets are the 5 × 5 on the cavity's second layer from the top.
  - `FluidAreaRender` can't be reused: it is typed to `WorkableMultiblockMachine` and reads recipe logic.
- Rod: `CustomItemStackHandler(1)` in the controller UI, filtered by `canPerformAction(FISHING_ROD_CAST)`, dropped
  via `modifyDrops`, never damaged. No rod is a start problem.
- Energy: while working, the controller drains energy inputs as fast as they give into `energyBuffer` (saved). The
  buffer is capped at Σ(V × A) × 100, saturating. It never power-fails. A cycle is 100 ticks. At the end of a cycle:
  - Cost per fish c = 1000 EU (×4 treasure) × (350 − min(Lure ticks, 300)) / 350, rounded up. Lure I/II/III give
    715/429/143.
  - m = buffer / (rolls × c). If m < 1, nothing happens and the buffer is kept.
  - Otherwise pay m × rolls × c, roll vanilla `FISHING` loot `rolls` times (1..64, set in the UI), and multiply
    every stack by m.
- Loot needs no AT or mixin.
  - Normal mode leaves out THIS_ENTITY, so the treasure entry's `in_open_water` condition fails (fish + junk).
  - Treasure mode passes a fresh `FishingHook`, not added to the level; `openWater` defaults to true.
  - Luck of the Sea → `withLuck(getFishingLuckBonus)`. The origin is the controller.
- Recipe (magical assembler, EV): 4 EV fishers, 8 EV circuits, 8 EV pumps, 4 EV robot arms, 32 watertight casings,
  32 double titanium plates, 16 fishing rods.

## Crystal Growth Chamber (`crystal/CrystalGrowth`, `crystal/CenterBlockCondition`)

Config `machines.crystalGrowthChamber`. Works in game on 1.21.

- Structure: 3 × 3 × 3 steel casing, controller in the middle of the front face, any block in the middle.
  - The middle of the four sides and the back: tempered glass or casing.
  - `NON_Y_AXIS`, no extended facing: the condition reads `pos.relative(front.getOpposite())`.
- Perfect subtick OC only: no parallel hatch (`autoAbilities(true, false, false)`), no batch mode (the user's call).
- Plain GT recipes (`gtmqol:crystal_growth`), one per budding block.
  - Each has `CenterBlockCondition(block)` plus the shard as a `notConsumable` input, and gives 4 shards, LV, 10 s.
  - The condition's `modifyUI` shows the block like GTCEu's `AdjacentBlockCondition`.
- Why the shard input:
  - `RecipeDB.addRecursive` keeps one recipe per set of item/fluid inputs and drops the rest (dev-only warning). The
    dropped ones aren't in the category map either, so not in EMI.
  - Recipes with no item/fluid input go into the category map but are never found by the lookup (`fromHolder`
    returns null).
  - Failing conditions are fine: `handleSearchingRecipes` goes on to the next match.
- Recipes are generated at runtime (`addRecipes`): `ae2:flawless_budding_quartz` → `certus_quartz_crystal`, plus
  every `<ns>:budding_<x>` that has an `<ns>:<x>_shard`, in any namespace.
  - That covers vanilla amethyst and all 28 GeOre crystals. GeOre's `GeOreBlockReg` registers exactly these names
    on both 1.20 and 1.21, and its cluster loot is the shard.
  - Missing ids are skipped.
- KubeJS: GTCEu registers a schema for every GT recipe type, so `event.recipes.gtmqol.crystal_growth(id)` exists. A
  binding for the condition is still to do.

## Greenhouse (`greenhouse/Greenhouse`, `integration/mysticalagriculture/MAGreenhouseRecipes`)

Config `machines.greenhouse`, covering both machines.

- Greenhouse: a single block in every `ELECTRIC_TIERS` tier (`<tier>_greenhouse`, `SimpleTieredMachine`, fermenter
  overlay), with non-perfect OC like GT's single blocks.
  - Crafted with `MetaTileEntityLoader.registerMachineRecipe` and `GTCraftingComponents`, shape `GSG/PMP/WCW` with a
    sapling. The fermenter's `WPW/GMG/WCW` without one collided.
  - Only tiers GT has components for get a recipe.
- Industrial Greenhouse, IV (the user's call, 2026-10-08):
  - 5 × 5 × 5: robust tungstensteel casing for the floor, edges and roof border; laminated glass (or casing) for the
    walls and roof.
  - The controller is in the front wall, one block above the floor.
  - The middle of the floor's inside must be `#minecraft:dirt` (`blockTag`); the rest of the inside is free.
  - Modifiers: `PARALLEL_HATCH`, `INDUSTRIAL_OUTPUT` (our own `RecipeModifier`, `outputModifier(multiplier(16))`:
    16× every output, chanced ones too) and perfect subtick OC. No batch mode.
  - Crafted from the IV greenhouse, IV pumps, LuV circuits and laminated glass.
- Plain recipes (`gtmqol:greenhouse`): the seed or sapling as a `notConsumable` input (distinct inputs, for the same
  reason as the crystal chamber) plus 1000 mB water, at `VA[LV]` so every tier runs every recipe.
  - Crops: 10 s. Seeds give 8 produce + 4 seeds. Plants that are their own seed (carrots, cane...) give 12.
  - Trees: 20 s. 16 logs, 8 leaves, 2 saplings, plus extras (apples, mangrove roots, shroomlights, sticky resin).
  - Mystical Agriculture (its API, `compileOnly` + dev runtime with Cucumber, from Modrinth): every enabled crop in
    `getCropRegistry().getCrops()`.
    - Tier 1 and elemental crops take 10 s; each tier above doubles that.
    - Output: 2 of its essence plus 1/10 fertilized essence (MA's own secondary drop).
    - No tier essences or seeds: those are crafted in MA.
- Sources, in order:
  - vanilla and GT rubber by hand;
  - then MA;
  - then other mods by guess. The guess skips anything that already has a recipe (`PLANTED`) and the `minecraft`
    namespace. It takes:
    - a `SaplingBlock` item `<x>_sapling` with an `<x>_log` (leaves too if `<x>_leaves` exists);
    - a `CropBlock` item `<x>_seeds` with an `<x>`;
    - any other `CropBlock` item as its own produce.
