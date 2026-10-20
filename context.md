# GTMQoL Current Context

Handoff notes for a new session. Build environment, dependency pitfalls and working rules are in `CLAUDE.md`; read
that first. This file is an index: what exists, where the details are, and what is pending. Details live in the doc index (see Docs).

## State

- Two long-lived branches, kept in sync with cherry-picks. Features go on both unless they are marked 1.21 only.
  - 1.21.1: `port/1.21.1` (this checkout). NeoForge 21.1, ModDevGradle, Java 21. Runs in game. Merged into `master`
    on 2026-10-13 (PR #1), so `master` is the 1.21.1 code now.
  - 1.20.1: `migrate/gtceu-v8-runtime-generation`. Forge 47, Java 17. Moved from Loom to ModDevGradle legacyForge
    (`3b63374`). `compileJava` and `runData` pass (2026-10-14, written ports included), not run in game yet.
- The user works on two computers, so the paths differ a little between them (this checkout is
  `C:\Users\yiran.wang\IdeaProjects\GTMQoL` on one and under `E:\code\Minecraft\` on the other). The 1.20.1 worktree
  sits next to this one, e.g. `..\GTMQoL-1.20.1`: look in the parent directory (or `git worktree list`) and check
  which branch it has checked out before porting.
- GTCEu v8 snapshot on both (pinned, see `gradle.properties`). Java only, no Kotlin.
- 1.20.1 sync (branch head 2026-10-12): everything up to "tag alias and AE Encoder fix", plus the written-not-built
  ports of auto-build, stack-like (Mek 10.4 chemicals, AE bridge, universal parts, pattern buffer), Mekanism
  gregification and Jade. The PB gregification and potion ports were written there too but are not on the branch.
  MI gregification is 1.21 only (MI has no Forge 1.20.1 build, `gregification.*`). That branch's `context.md` is
  older; treat this one as the reference.
- Since 2026-10-17 both branches also have (compiled and `runData`'d on 1.20.1, run in ATM10 on 1.21): ghost
  ingredients, Create gregification, pattern buffer shared hatches and output marks, `MarkCoveredRecipes`,
  composable single blocks, runtime wire tags, the PB entity-thread fix.
- The old implementation is archived under `reference/` as research material. Do not migrate it wholesale and do not
  modify it.

## Docs

Design docs live in the agent-toolkit doc index (`.agent-index.db`, use the `index_*` tools; never read the file). Entries point at the code; search with `index_find`. Id prefixes:

| Prefix | Covers |
|---|---|
| `gregification.*` | foreign recipe types on GT machines, MI/Mek/PB/Create sources, composable single blocks |
| `stacklike.*` | stack-like recipe caps (chemicals), AE bridges, universal ME parts, EMI/Jade |
| `ghost.*` | ghost ingredients, hatch, single block marks, MarkCoveredRecipes |
| `multiblock.*`, `autobuild.*` | our multiblocks; survival auto-build |
| `steam.*` | steam multis, hatches, control circuits, early game |
| `ae2.*`, `wireless.*` | AE2 features; wireless networks |
| `recipedb.*`, `recipes.*`, `oc.*` | grouped search; recipe sources, circuits, tags; overclocking |
| `potions.*`, `modular.*` | potion fixes; modular multiblocks |
| `port.*`, `gtceu-upgrade.*` | gtceufix workarounds, 1.20.1 differences; bumping GTCEu |
| `config.*`, `generation.*`, `tests.*` | config/early config/datagen; runtime generation; integration tests |

## Source layout (1.21.1)

Loosely follows GTCEu Modern (`api` / `common` / `data` / `core` / `integration` / `client` / `config`). Features are
kept together as subpackages of `common/`, not split by kind the way GTCEu does.

```text
src/main/java/com/yiran/minecraft/gtmqol/
├── GTMQoL.java            entry point, GTMQoL.id(), what gets registered (config gated), init order
├── GTMQoLAddon.java       @GTAddon, owns the GTRegistrate, creative tab, machine()/multiblock() helpers, addRecipes
├── api/generation/        runtime (dynamic) resource generation, opt-in per machine
├── client/                client-only init and renders (DTFR ring, fishing pond water, Create casing parts)
├── config/                GTMQoLConfig (toma, runtime), EarlyConfig (properties, mixin time)
├── common/
│   ├── assembler/         MagicalAssembler and its UI
│   ├── autobuild/         survival auto-build
│   ├── circuit/           UniversalCircuits, ControlCircuits
│   ├── crystal/           crystal growth chamber
│   ├── fe/                FEInputProvider
│   ├── ghost/             ghost ingredient capability, hatch, list UI, single block marks trait
│   ├── greenhouse/        greenhouse + industrial greenhouse
│   ├── implosion/         electric implosion compressor
│   ├── modular/           modular multiblock versions of single blocks
│   ├── multiblock/        SAF, DTFR, cyclic multiblocks (void miner, fishing pond)
│   ├── overclock/         replacement OC logics
│   ├── part/              LegacyPartRecipes (extruder molds v8 removed)
│   ├── potion/            PotionContainers (splash/lingering marker on potion fluids)
│   ├── recipedb/          non-mixin side of the grouped search
│   ├── singleblock/       SingleblockFeature, ComposableTieredMachine, BatchModeTrait
│   ├── stacklike/         GenericStackLikeType/RecipeCapability/handler; mekanism/ = chemicals
│   ├── steam/             advanced steam multis, steam parallel hatch, steam magical assembler
│   ├── test/              IntegrationTests
│   └── wireless/          binding, networks, covers; steam/ and energy/
├── gregification/         Gregification, ForeignMachineType, GregifiedRecipeType, modifiers, MarkCoveredRecipes;
│                          mi/, mekanism/, productivebees/, create/, client/
├── data/                  recipe/ (MiscRecipes, EarlyGameRecipes, WirelessRecipes), tag/ (CircuitTags, MaterialAliasTags,
│                          WireTags)
├── core/                  mixin config plugins (GTMQoL, RecipeDB, EAP, AE2, JeiRecipeSlotFix)
│   └── mixins/            gtmqol.mixins.json; gtceufix/, recipedb/, eap/, ae2/ have their own notes in the docs
└── integration/           ae2/ (+ stacklike/ bridges), jade/, mysticalagriculture/, productivebees/
```

## Feature summary

- **Magical assembler**: always-on 16-item/4-fluid assembler used by most of our recipes. → `recipes.*`
- **Wireless steam/EU** per team/private network, hatches, accessors, monitors, covers; FE input on every GT
  machine/cable. → `wireless.*`
- **Modular machines**: a 3×3×3 multiblock per single-block recipe type set, generated at runtime.
  → `modular.*`
- **Overclocking** overhaul, fusion buff, tier skipping. → `oc.*`
- **Multiblocks**: SAF, DTFR, electric implosion, void miner, fishing pond, crystal growth, greenhouse.
  → `multiblock.*`
- **Steam**: GTNL-style large steam multis, extra steam single blocks, control circuits, early game. → `steam.*`
- **AE2**: 216-slot buffer, shared hatches and pattern output marks, ME processing, smart doubling, dual/universal
  parts, buffer return, sticky card. → `ae2.*`
- **RecipeDB** grouped search. → `recipedb.*`
- **Stack-like capabilities**: Mekanism chemicals as a real recipe capability, in ME parts, pattern buffers, EMI and
  Jade. → `stacklike.*`
- **Gregification**: MI, Mekanism (23 machines), Productive Bees (centrifuge, incubator, gene extractor, beehive,
  breeder, canner bee logic) and Create (12 machines incl. the sequenced assembly line) recipes on gregified GT
  machines; composable single blocks. → `gregification.*`
- **Ghost ingredients**: never-consumed marks that pick a recipe among ones with the same inputs; Ghost Ingredient
  Hatch, marks list on single blocks. → `ghost.*`
- **Survival auto-build**: a button on every multiblock controller that builds from the player's inventory, with a
  settings window. → `autobuild.*`
- **Potions**: gtceu's brewery/canner logic working on 1.21, potion fluid names, splash/lingering potions in the
  brewery. → `potions.*`
- **Legacy part recipes**: the rod/bolt/gear/small gear/rotor extruder molds v8 removed, plus a long rod mold.
  → `recipes.*`
- **gtceu bug workarounds** (`core/mixins/gtceufix/`). → `port.*`

## Pending / open

Not built or not checked in game yet (1.21):

- PB: breeder, configurable comb fix, fluid flowers, ore-bee recipes, canner both ways (`gregification.*`).
- Mek: the chemical converter and the 20 types added in one batch (`gregification.*`).
- Potions: fluid names, EMI potion inputs, splash/lingering (`potions.*`). The "Any mod's version works" tooltip
  moved to its own mixin (`GTRecipeViewerWidgetPotionMixin`).
- SAF: 1-9 data hatches, any one having the research is enough; `machines.easySAFRecipe` (both branches, not
  built; `multiblock.*`).
- Crystal growth chamber on the block capability (`common/blockprovider/`, both branches built + datagen'd, not run;
  `multiblock.*`): middle block as the recipe's only input, EMI/JEI lookup, KubeJS plugin.
- Lathe lubricant on the large steam lathe and modular lathe (`WorkableMultiblockMachineMixin`, both branches, not
  built; `steam.*`).
- Legacy part recipes (both branches, not built; needs `runData` for models/lang): molds craft and copy, extruder
  recipes per material, rotor recycles into 4 ingots (`recipes.*`).
- Random idle with wireless hatches (both branches): checks now see the tick's whole budget (`wireless.*`), plus
  `gtceufix/RecipeLogicMixin` (lost `lastOriginRecipe`) and `gtceufix/RecipeRunnerMixin` (chance overflow)
  (`port.*`).
- EMI real slots for chemicals: icons/amounts, R/U, U finding GT recipes, AE2 "+" auto-fill, no duplicate tooltips.
- The dedicated-server path of gregification (`GregificationClient`).
- Create fan from Create's fan processing registry (both branches, built, not run; `gregification.*`): addon kinds
  (Dragons Plus freezing/sanding/ending), catalysts from the `fan_processing_catalysts` tags through `LoadingTags`
  (which MI's `GTMaterialTags` now uses too). 1.20.1 clients redo the conversion when tags arrive.
- Grouped search safety (both branches, built, not run; `recipedb.*`): only plain RHLs and gtceu's/our
  buffers are restricted, anything else combines with everything. Also the shared hatches / output marks fix of
  2026-10-18.
- KubeJS machines registered after the modular machines are declared; electric implosion copies of KubeJS recipes
  (1.20.1 has no late regeneration, so none there).
- `gtceufix/AbstractWidgetMenuMixin`.
- Universal ME parts recipe: dual part + 16k ME storage component (was dual + ME bus), both branches; needs `runData`.
- Cleanup of 2026-10-14 (both branches): the wireless rework (`WirelessPool`, `WirelessUI`, `WirelessMonitorMachine`,
  `WirelessCover`; the saved NBT is unchanged, old worlds should load), `StockingParts` (a gtceu stocking hatch now
  also refuses a fluid a dual input stocks), ranged fluid outputs into ME parts (1.21 used the ingredient's amount
  instead of the max roll), the steam multis now use our OC (×4 EU/t, ÷4 duration with the overhaul).
- ME dual input is now the 36-slot universal input for items and fluids; old parts and old data stick copies merge
  their item and fluid config on load (`ae2.*`). Both branches; lang regenerated.
- GTCEu bumped to the 2026-10-13/14 snapshots on both branches, with MUI (`gtceu-upgrade.*`): auto-build's
  predicate roots are now our own map (GTCEu dropped `getParent`), 1.20.1 got `RecipeRunnerMixin` and
  `RecipeLogicMixin`. Check auto-build block choices, the void miner and the steam macerator in game.

- 1.20.1: everything in the "Since 2026-10-17" line above compiles and `runData` passes, but nothing of it has run
  in game there.

To do:

- 1.20.1: compiles, and `runData` runs and exits with nothing to regenerate (2026-10-14). Next a test in game, then
  the PB and potion ports.
- Auto-build: substructures, then block choices for expandable patterns (`autobuild.*`).
- Crystal growth: a KubeJS binding for `CenterBlockCondition`.
- Stack-like: external chemical hatches.
- Abstractions the user agreed to but not done yet: a base for the mixin plugins' empty `IMixinConfigPlugin`
  methods, the cyclic multiblocks' popup helpers. (`FluidConfigWidget` is gone with the dual input rework.)

Known, not fixed:

- Proxy pattern buffers don't get the pattern output marks for the recipe lookup (`ae2.*`).

- The user said leave it for now; the workaround is not to reload client resources, and to restart if it happens.
  - After a client resource reload, every GTCEu bronze/steel themed UI fails to open with
    `ClassCastException: IDrawable$2 (NONE) cannot be cast to UITexture` in `MachineUIPanel.<init>`.
  - Cause: ModularUI 3.3.1-SNAPSHOT's `ThemeManager` merges the java theme with a shallow `addAllOf`. `parse` then
    writes `background: "none"` into GTCEu's registered `GTGuiTheme` JSON.
  - Possible fix: a client mixin in `gtceufix/` that passes `builder.getJson().deepCopy()`.
- Upstream: steam panel title and logo squeezed (`steam.*`). GTCEu v8 terminal auto-build only works in creative,
  and only after the preview has been opened and closed once.

Open questions for the user:

- The user dislikes that two stocking parts of one multiblock can't stock the same key (gtceu's rule, which
  `StockingParts` extends to ours), but has no better idea yet: both would see the full network amount and the
  recipe check would count it twice. Revisit if a way to share it comes up.

- Should the wireless accessor use the rainbow overlay?
- Maintenance hatches on our multis: all other abilities are used. Nothing removed yet.

javac runs with `-Xlint:deprecation,removal,unchecked,rawtypes`. 1.21 has no warnings; 1.20.1 only Forge's deprecation
of `BuiltInRegistries.ITEM/BLOCK/...` (kept so the code stays the same as 1.21). Keep it that way.

Jar naming: `gtmqol-<mod version>-<mc version>.jar`. `build.gradle` sets `archiveVersion` on every
`AbstractArchiveTask`; `project.version` stays `mod_version`.

## User preferences not covered by `CLAUDE.md`

- `README.md` is entirely in English. Credits for borrowed ideas and assets go in its Credits section, never in code
  comments.
- The user replies in Chinese.
- The user runs all Gradle tasks (build, `runData`, `runClient`) and pastes logs back; tell them exactly what to look
  for in game.
