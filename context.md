# GTMQoL Current Context

Handoff notes for a new session. Build environment, dependency pitfalls and working rules are in `CLAUDE.md`; read
that first. This file is an index: what exists, where the details are, and what is pending. Details live in `docs/`.

## State

- Two long-lived branches, kept in sync with cherry-picks. Features go on both unless they are marked 1.21 only.
  - 1.21.1: `port/1.21.1` (this checkout). NeoForge 21.1, ModDevGradle, Java 21. Runs in game.
  - 1.20.1: `migrate/gtceu-v8-runtime-generation` (worktree `E:\code\Minecraft\GTMQoL-1.20.1`). Forge 47, Loom 1.13,
    Java 17. `master` is stale.
- GTCEu v8 snapshot on both (pinned, see `gradle.properties`). Java only, no Kotlin.
- 1.21 only so far: stack-like capabilities (Mekanism chemicals), gregification, the universal ME parts, the
  restructured source layout (1.20.1 still has the old flat layout; apply the move there before cherry-picking).
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
├── gregification/         Gregification, ForeignMachineType, GregifiedRecipeType, modifiers; mi/, mekanism/, client/
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
- **Gregification** (1.21): MI and Mekanism machine recipes on gregified GT machines. → `GREGIFICATION.md`

## Mekanism gregification (1.21, `gregification/mekanism/MekanismGregification`)

Config `gregification.mekanism`. Design and durations (per-tick machines: 1 t, unscaled) are in `GREGIFICATION.md`.

- 22 `mek_*` multiblocks (`MultiblockShape.ANY_PARTS`): crusher, enrichment chamber, osmium compressor, purification
  chamber, chemical injection chamber, metallurgic infuser, painting machine, dissolution chamber, combiner,
  precision sawmill, crystallizer, oxidizer, pigment extractor, PRC, chemical infuser, pigment mixer, isotopic
  centrifuge, solar neutron activator, washer, electrolytic separator, rotary condensentrator + decondensentrator,
  thermal evaporation plant.
- Crafted from the Mek machine with a GT hammer or through the magical assembler (circuit 5). The decondensentrator
  is crafted from our condensentrator.
- Not covered on purpose: Mek smelting, fission, fusion, SPS, pumps, antimatter/nucleosynthesizer, energy/chemical
  conversion.
- None of the 20 new types has been run in game yet.

## Pending / open

- Not verified in game (1.21):
  - EMI real slots for chemicals: icons/amounts, R/U, U finding GT recipes, AE2 "+" auto-fill, no duplicate tooltips.
  - The dedicated-server path of gregification (`GregificationClient`).
  - KubeJS machines registered after the modular machines are declared.
  - Electric implosion copies of KubeJS recipes (1.20.1 has no late regeneration, so none there).
- Crystal growth: a KubeJS binding for `CenterBlockCondition`.
- Stack-like: external chemical hatches; the 1.20.1 port.
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
