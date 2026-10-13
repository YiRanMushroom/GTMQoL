# GTMQoL Current Context

Handoff notes for a new session. Build environment, dependency pitfalls and working rules are in `CLAUDE.md`; read
that first. This file is an index: what exists, where the details are, and what is pending. Details live in `docs/`.

## State

- Two long-lived branches, kept in sync with cherry-picks. Features go on both unless they are marked 1.21 only.
  - 1.21.1: `port/1.21.1` (this checkout). NeoForge 21.1, ModDevGradle, Java 21. Runs in game.
  - 1.20.1: `migrate/gtceu-v8-runtime-generation` (worktree `E:\code\Minecraft\GTMQoL-1.20.1`). Forge 47, Loom 1.13,
    Java 17. `master` is stale.
- GTCEu v8 snapshot on both (pinned, see `gradle.properties`). Java only, no Kotlin.
- 1.21 only so far: stack-like capabilities (Mekanism chemicals), gregification, the universal ME parts, Jade.
  1.20.1 has everything up to "tag alias and AE Encoder fix" (synced by hand, same source layout as here). Its
  `context.md` is older; treat this one as the reference.
- The old implementation is archived under `reference/` as research material. Do not migrate it wholesale and do not
  modify it.

## Docs

| Doc | Covers |
|---|---|
| `docs/PORT_1_21.md` | 1.21 build setup, code differences vs 1.20.1, every `gtceufix/` workaround |
| `docs/CONFIG.md` | toma config, all toggles, early config, datagen vs runtime resources, datagen gotchas |
| `docs/RECIPES.md` | where recipes come from, magical assembler + its UI, circuits, `CircuitTags`, `MaterialAliasTags`, wireless recipes |
| `docs/STACK_LIKE_CAPABILITIES.md` | generic stack-like recipe caps (chemicals), AE bridges, universal ME parts, pattern buffer, EMI/Jade display, how to add a type |
| `docs/GREGIFICATION.md` | foreign recipe types on GT machines: flow, pieces, modifiers, crafting, dedicated server, MI/Mek sources, how to add a source |
| `docs/MULTIBLOCKS.md` | SAF, DTFR, electric implosion + nether stars, cyclic multiblocks, void miner, fishing pond, crystal growth, greenhouse |
| `docs/STEAM.md` | advanced steam multis, steam parallel hatch, control circuits, steam single block IO/ghost circuits, early game, manual compression |
| `docs/OVERCLOCKING.md` | OC overhaul (`@Overwrite`s), fusion buff, multi tier skipping |
| `docs/MODULAR_MACHINES.md` | which machines get a modular multiblock and how |
| `docs/WIRELESS.md` | wireless networks/binding, steam and EU networks, covers, EU ↔ FE |
| `docs/AE2.md` | overclocked pattern buffer, ME processing, smart doubling, dual/universal parts, buffer return, sticky card, encoding |
| `docs/RECIPEDB_REFACTOR.md` | RecipeDB grouped search (late-game lag fix) |
| `docs/DYNAMIC_GENERATION.md`, `docs/INTEGRATION_TESTS.md` | runtime resource generation background (early, may be partly stale) |

## Source layout (1.21.1)

Loosely follows GTCEu Modern (`api` / `common` / `data` / `core` / `integration` / `client` / `config`). Features are
kept together as subpackages of `common/`, not split by kind the way GTCEu does.

```text
src/main/java/com/yiran/minecraft/gtmqol/
├── GTMQoL.java            entry point, GTMQoL.id(), what gets registered (config gated), init order
├── GTMQoLAddon.java       @GTAddon, owns the GTRegistrate, creative tab, machine()/multiblock() helpers, addRecipes
├── api/generation/        runtime (dynamic) resource generation, opt-in per machine
├── client/                client-only init and renders (DTFR ring, fishing pond water)
├── config/                GTMQoLConfig (toma, runtime), EarlyConfig (properties, mixin time)
├── common/
│   ├── assembler/         MagicalAssembler and its UI
│   ├── circuit/           UniversalCircuits, ControlCircuits
│   ├── crystal/           crystal growth chamber
│   ├── fe/                FEInputProvider
│   ├── greenhouse/        greenhouse + industrial greenhouse
│   ├── implosion/         electric implosion compressor
│   ├── modular/           modular multiblock versions of single blocks
│   ├── multiblock/        SAF, DTFR, cyclic multiblocks (void miner, fishing pond)
│   ├── overclock/         replacement OC logics
│   ├── recipedb/          non-mixin side of the grouped search
│   ├── stacklike/         GenericStackLikeType/RecipeCapability/handler; mekanism/ = chemicals
│   ├── steam/             advanced steam multis, steam parallel hatch, steam magical assembler
│   ├── test/              IntegrationTests
│   └── wireless/          binding, networks, covers; steam/ and energy/
├── gregification/         Gregification, ForeignMachineType, GregifiedRecipeType, modifiers; mi/, mekanism/, productivebees/, client/
├── data/                  recipe/ (MiscRecipes, EarlyGameRecipes, WirelessRecipes), tag/ (CircuitTags, MaterialAliasTags)
├── core/                  mixin config plugins (GTMQoL, RecipeDB, EAP, AE2, JeiRecipeSlotFix)
│   └── mixins/            gtmqol.mixins.json; gtceufix/, recipedb/, eap/, ae2/ have their own notes in the docs
└── integration/           ae2/ (+ stacklike/ bridges), jade/, mysticalagriculture/
```

## Feature summary

- **Magical assembler**: always-on 16-item/4-fluid assembler used by most of our recipes. → `RECIPES.md`
- **Wireless steam/EU** per team/private network, hatches, accessors, monitors, covers; FE input on every GT
  machine/cable. → `WIRELESS.md`
- **Modular machines**: a 3×3×3 multiblock per single-block recipe type set, generated at runtime.
  → `MODULAR_MACHINES.md`
- **Overclocking** overhaul, fusion buff, tier skipping. → `OVERCLOCKING.md`
- **Multiblocks**: SAF, DTFR, electric implosion, void miner, fishing pond, crystal growth, greenhouse.
  → `MULTIBLOCKS.md`
- **Steam**: GTNL-style large steam multis, extra steam single blocks, control circuits, early game. → `STEAM.md`
- **AE2**: 216-slot buffer, ME processing, smart doubling, dual/universal parts, buffer return, sticky card. → `AE2.md`
- **RecipeDB** grouped search. → `RECIPEDB_REFACTOR.md`
- **Stack-like capabilities** (1.21): Mekanism chemicals as a real recipe capability, in ME parts, pattern buffers,
  EMI and Jade. → `STACK_LIKE_CAPABILITIES.md`
- **Gregification** (1.21): MI, Mekanism and Productive Bees machine recipes on gregified GT machines.
  → `GREGIFICATION.md`
- **Survival auto-build** (both branches, `common/autobuild/`, config `misc.autoBuild`; 1.20.1 differences in
  `docs/PORT_1_21.md`): a terminal-icon button on every
  multiblock controller (`MachineUIPanelBuilderMixin`). Left click places up to 64 blocks from the player's
  inventory, shift+left toggles a job (64 blocks every 5 ticks), right click opens the settings window.
  - `StructurePlanner`: GTCEu's structure helpers (anonymous subclasses override `getMinCount`/`getMaxCount`) with
    ability predicates at 0 (type name `Ability`/`TieredAbility`); ability-only positions are left empty. Sorted by
    y, then distance to the controller. Logs planning time.
  - `AutoBuilder`: places only into air/replaceable blocks (anything solid counts as done), fires
    `EntityPlaceEvent`, `checkAndFormStructure` only at the end, reports missing items. Jobs are keyed by
    dimension + controller pos and stop when done, formed, stuck, player away/offline, or controller gone.
  - `BuildSettings`: slice repeats, expandable bounds, block choices (pattern char → index in `root.expand()` +
    block). Transient client-side data per multiblock definition id (`AutoBuildPanel.SETTINGS`), sent with every
    click through a C2S `SyncHandler` (`AutoBuildButton.BuildRequest`). Only `DEFAULT_STRUCTURE`, no substructures.
  - `BuildOptions`: what the window offers, computed the same way on both sides. Slices with min < max, expandable
    bounds, and chars (not air/any/controller) whose non-ability predicates have at least 2 distinct blocks with
    an item. The server ignores choices not in it and clamps repeats/bounds.
  - A choice puts the `BlockInfo` into `getBlockPreferences()` keyed by the node that holds the predicate (GTCEu
    looks up by the node it walks, not the root) and gives the root's other non-ability predicates counts of 0.
  - `AutoBuildPanel`: `IPanelHandler.simple` sub panel centered on screen, no synced values, no 3D preview (GTCEu's
    preview is buggy). The user doesn't want implementation details (tick counts, "kept until restart") in the UI
    text.
    Sliders + item toggle buttons; selecting a selected toggle goes back to the default. Expandable patterns only
    get size sliders for now.

## Mekanism gregification (1.21, `gregification/mekanism/MekanismGregification`)

Config `gregification.mekanism`. Design and durations (per-tick machines: 1 t, unscaled) are in `GREGIFICATION.md`.

- 22 `mek_*` multiblocks (plus the chemical converter, below) (`MultiblockShape.ANY_PARTS`): crusher, enrichment chamber, osmium compressor, purification
  chamber, chemical injection chamber, metallurgic infuser, painting machine, dissolution chamber, combiner,
  precision sawmill, crystallizer, oxidizer, pigment extractor, PRC, chemical infuser, pigment mixer, isotopic
  centrifuge, solar neutron activator, washer, electrolytic separator, rotary condensentrator (both directions in
  one machine), thermal evaporation plant.
- Crafted from the Mek machine with a GT hammer or through the magical assembler (circuit 5).
- Not covered on purpose: Mek smelting, fission, fusion, SPS, pumps, antimatter/nucleosynthesizer, energy/chemical
  conversion.
- `mek_chemical_converter` (23rd) proxies `mekanism:chemical_conversion` (item → chemical, e.g. the osmium
  compressor's osmium from ingots/blocks). It is crafted from our `mek_chemical_oxidizer`, not Mek's oxidizer, so the
  two hammer recipes don't clash. It is a separate machine because conversion mostly duplicates oxidizing, and on salt
  and sulfur the two give different outputs for the same input: in one GT type, gtceu's staging set would keep a
  random one. Duration is the oxidizer's. Not built yet.
- None of the 20 new types has been run in game yet.

## Productive Bees gregification (1.21, `gregification/productivebees/`)

Config `gregification.productiveBees`. Design in `GREGIFICATION.md`. Comb centrifuge (converted PB recipes),
industrial incubator, gene extractor and beehive (custom recipe logic, no proxy); the GT canner moves bees between
cages and spawn eggs (genes kept). The PB machines, the beehive layout (upgrades column, then bee/flower/fluid) and
its machine tooltip were checked in game. The capability alias fix for PB's bundled old-format gtceu recipes is in
`integration/productivebees/`. Sturdy cages work everywhere (`BeeCage` subclass).
Industrial breeder (`BreederLogic`, not built yet): like the breeding chamber, two caged adults (not used up), each
parent's breeding items and an empty cage give a caged baby; several possible offspring are XOR chanced outputs (16 output slots in a 4x4 grid, since gtceu wants room for
every chanced output; one EMI recipe per parent pair). Inputs are always parent, parent, empty cage, food, food, so
`BreederUI` can show the cages and the breeding items as two columns.
Configurable combs (PB's `productivebees:component`, a partial `DataComponentIngredient`): gtceu's recipe lookup
key compares component maps instead of testing them, so recipes asking for a configurable comb were never found,
including PB's own Mek oxidizer recipes (wasted radioactive comb). Fixed for every recipe type by
`gtceufix/ItemDataComponentMapIngredientMixin` (partial keys of the same item match when either predicate accepts the
other's stack); the centrifuge converter keeps PB's ingredient as is. Not built yet.
Ported to 1.20.1 (PB 1.20.1-12.6.0), not built yet; the differences are in that branch's `context.md`. Possible
later: gene sampler upgrade in the beehive, gene-applying treats in the incubator.

## Pending / open

- PB gregification: check that PB's ore-bee gtceu recipes (`gtceu:alloy_smelter/barite_bee` etc.) load without
  `Unknown registry key ... minecraft:item`; canner cage ↔ egg both ways (genes kept), beehive slot layout
  (upgrades column | bee, flower, fluid column | outputs, in machine UI and EMI; "works like PB" machine tooltip) and
  flower requirement (item / bucket / 1000 mB fluid, EMI shows each bee's flower).
- Not verified in game (1.21):
  - EMI real slots for chemicals: icons/amounts, R/U, U finding GT recipes, AE2 "+" auto-fill, no duplicate tooltips.
  - The dedicated-server path of gregification (`GregificationClient`).
  - KubeJS machines registered after the modular machines are declared.
  - Electric implosion copies of KubeJS recipes (1.20.1 has no late regeneration, so none there).
- Crystal growth: a KubeJS binding for `CenterBlockCondition`.
- Survival auto-build: placing and the settings window work in game (1.21); `AbstractWidgetMenuMixin` not verified.
  The 1.20.1 port is written, not built or run yet. Next: substructures, then block choices for expandable
  patterns.
- gtceu custom recipe logic (1.21, `gtceufix/GTRecipeTypeMixin`): forming press naming verified in game.
  Brewery output components: `gtceufix/GTRecipeBuilderMixin` (recipe) built; `gtceufix/GTEmiRecipeMixin` (EMI R
  lookup) and canner potion bottling/draining in EMI (`CannerLogicMixin`) verified in game. Both ported to 1.20.1
  (not built); 1.20.1 doesn't need the other two. Potion fluid inputs showed Uncraftable Potion in EMI (brewery,
  canner filling): gtceu maps a component fluid ingredient over the fluid's tag to a tag list, and EMI's
  `EmiTags.getIngredient` merges potion + flowing potion back into `c:potion` by fluid alone. `gtceufix/
  FluidRecipeCapabilityMixin` lists only the source fluids instead; both branches, not built. Potion fluid names:
  `gtceufix/PotionFluidTypeMixin` (1.21 only) uses the potion item's keys (gtceu's own `fluid.gtceu.potion.effect.*`
  have no translations); `PotionFluidNameMixin` (both) appends the first effect's potency and duration so normal /
  long / strong differ. Not built. In ATM10 the brewery EMI input was still wrong: other mods' potion fluids
  (Immersive Engineering first, Create) are in `c:potion` too, so several sources were left and EMI merged them again;
  pattern transfer took IE's. The mixin now lists a single fluid, gtceu's if present (user's choice; display only,
  gtceu's brewery still takes other mods' potions through the tag). `GTRecipeViewerWidgetMixin` adds "Any mod's
  version works" to such an input slot's tooltip when the tag has other mods' fluids (lang in `PotionContainers`,
  needs `runData`). 1.21 only, not built; port to 1.20.1 after.
  Splash / lingering potions are made in the brewery (user's choice; a canner item-to-item version was written and
  removed). `common/potion/PotionContainers`: data component `gtmqol:potion_container` (the potion Item) on gtceu's
  potion fluid, absent = plain potion. `BreweryLogicMixin`: vanilla's container mixes (from
  `PotionBrewingContainerMixesAccessor`, `PotionBrewing$Mix` opened by our AT) on potion fluids, 750 mB like gtceu,
  when gtceu found nothing; EMI recipes per potion. Dragon's breath's bottle is lost (brewery has no item output).
  Marked fluids aren't brewed further (gtceu compares all components). `BottleItemFluidHandlerMixin` (fill into the
  marked item), `PotionItemFluidHandlerMixin` (splash / lingering drain to the marked fluid), canner EMI fill / drain
  for each container, `PotionFluidNameMixin` names them like the item. 1.21 only, not built; 1.20.1 port after.
- Universal ME parts recipe: dual part + 16k ME storage component (was dual + ME bus), both branches; needs
  `runData`.
- Stack-like: external chemical hatches. The 1.20.1 port (stack-like, Mek chemicals, gregification
  + Mek machines, AE/pattern buffer, Jade) is fully written but not built or run yet; next is fixing compile errors one
  at a time, then `runData` for the universal parts (see `docs/STACK_LIKE_CAPABILITIES.md`).
- Known bug, not fixed. The user said leave it for now; the workaround is not to reload client resources, and to
  restart if it happens.
  - After a client resource reload, every GTCEu bronze/steel themed UI fails to open with
    `ClassCastException: IDrawable$2 (NONE) cannot be cast to UITexture` in `MachineUIPanel.<init>`.
  - Cause: ModularUI 3.3.1-SNAPSHOT's `ThemeManager` merges the java theme with a shallow `addAllOf`. `parse` then
    writes `background: "none"` into GTCEu's registered `GTGuiTheme` JSON.
  - Possible fix: a client mixin in `gtceufix/` that passes `builder.getJson().deepCopy()`.
- Known, upstream: steam panel title and logo squeezed (`STEAM.md`). GTCEu v8 terminal auto-build only works in
  creative, and only after the preview has been opened and closed once.
- Open questions for the user:
  - Should the wireless accessor use the rainbow overlay?
  - Maintenance hatches on our multis: all other abilities are used. Nothing removed yet.
- Jar naming: `gtmqol-<mod version>-<mc version>.jar`. `build.gradle` sets `archiveVersion` on every
  `AbstractArchiveTask`; `project.version` stays `mod_version`.

## User preferences not covered by `CLAUDE.md`

- `README.md` is entirely in English. Credits for borrowed ideas and assets go in its Credits section, never in code
  comments.
- The user replies in Chinese.
- The user runs all Gradle tasks (build, `runData`, `runClient`) and pastes logs back; tell them exactly what to look
  for in game.
