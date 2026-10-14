# Config and resources

Split out of `context.md`.

## Runtime config (`config/GTMQoLConfig`, toma, `config/gtmqol.yaml`)

- We use toma's `Configuration` (YAML), not `ForgeConfigSpec`/`ModConfigSpec`, because values are needed during
  CONSTRUCT (`CLAUDE.md`).
- Always read through `GTMQoLConfig.get()`, which registers the config on first use, like GTCEu's `ConfigHolder`.
  - The fusion, output limit and boiler mixins read it from GTCEu's static initializers. Those run while GTCEu is
    constructed, before our constructor.
  - toma's `registerConfig` only needs the `@Config` id, not a mod loading context.
- During datagen, `get()` returns a fresh `new GTMQoLConfig()` (all defaults, nothing read or written). So datagen
  always generates lang/models for every feature; turning a feature off only skips registration at runtime.
- Every feature has a toggle, all default true and restart-only, except:
  - `integrationTests.enabled` (default false). It registers `gtmqol:runtime_single_block` and
    `gtmqol:runtime_multiblock`, never during datagen.
  - `ae2.patternBufferReturn` and `ae2.skipNotConsumedInputs`, which are read at runtime.

| Group | Keys |
|---|---|
| `machines.*` | `smartAssemblyFactory`, `dimensionallyTranscendentFusionReactor`, `voidMiner`, `fishingPond`, `crystalGrowthChamber`, `greenhouse`, `advancedSteamMachines` (one unit), `electricImplosionCompressor` |
| `modularMachines.enabled` | `MODULAR_MACHINES.md` |
| `gregification.*` | `modernIndustrialization`, `mekanism` |
| `wireless.energy` / `wireless.steam` | machines, covers, recipes. `WirelessNetworks.init` (events, command, stats) always runs; the void miner uses the EU network too |
| `circuits.*` | `universalCircuits` (items + the magical assembler conversion), `controlCircuits`, `mekanismCircuitTags` |
| `recipes.*` | `miscRecipes`, `earlyGame` (plus the kept sand → glass), `keepManualCompression`, `keepVanillaTNT`, `netherStarDust` |
| `steamTweaks.*` | `circuitSlots`, `fluidTanks`, `noOutputLimits`, `optionalLargeBoilerParts` |
| `overclocking.*` | `buffFusionReactor`, `enableMultiTierSkipping` (`OVERCLOCKING.md`) |
| `ae2.*` | `overclockedPatternBuffer`, `processing`, `dualHatches`, `patternBufferReturn`, `skipNotConsumedInputs` |
| `misc.feInput` | |
| `voidMiner.dimensionMapping` | not a toggle |

The authoritative list is `GTMQoLConfig` itself; this table may lag.

Always on: the magical assembler and its own recipes (creative data hatch, rubber sapling), and the GTCEu bug
fixes. Mixins that are not `@Overwrite`s always apply and check the config at run time.

## Early config (`config/EarlyConfig`, `config/gtmqol-early.properties`)

Mixin configs load before mods, so mixin-time options can't use toma. These go in a `java.util.Properties` file,
the same approach as GTCEu's `gtceu-early.properties`. Missing keys are added with their defaults.

- `recipeDB.groupedSearch` (default true): `core/RecipeDBMixinPlugin` applies the whole recipedb config or not.
- `overclocking.overhaul` (default true): `core/GTMQoLMixinPlugin` skips `OverclockingLogicMixin`,
  `GTRecipeViewerWidgetMixin` and `GTRecipeModifiersMixin` when it is off.
- `ae2.universalCircuitEncoding`, `ae2.stickyCard` (`AE2.md`).

## Resources: datagen vs runtime generation

Datagen is the default.

- **Datagen.** Machines registered through `GTMQoLAddon.registrate()` get blockstates, models and lang from
  Registrate when the user runs `runData`.
  - Lang comes from `.langValue(...)` on builders, and from `registrate().addRawLang(key, value)` for everything else.
  - After changing models or lang, re-run `runData` before `runClient`.
- **Runtime generation (opt-in).** `GTMQoLAddon.machine(...)` / `multiblock(...)` return builders with
  `.dynamicallyGenerated(true)`.
  - `MachineBuilderMixin` then queues the builder in `RuntimeGeneration`, which writes models/lang into GTCEu's
    `GTDynamicResourcePack` on every `RegisterDynamicResourcesEvent`.
  - Extra lang: `RuntimeGeneration.addLanguageEntry`.
  - Used by the modular machines, the gregified machines/recipe types and the integration-test machines. Background
    in `DYNAMIC_GENERATION.md` and `INTEGRATION_TESTS.md` (written early; may be partly out of date).
- Generated resources are committed under `src/generated/resources`. Static textures are under
  `src/main/resources/assets/gtmqol/`.
- 1.21: `src/generated/resources` may still have the 1.20 layout (`data/forge/tags/items`); regenerate with `runData`.

## Datagen gotchas

- Fixed: GTCEu instantiates addons while it is itself being constructed.
  - So a `GTRegistrate.create(MOD_ID)` in `GTMQoLAddon`'s static init hooked Registrate's `GatherDataEvent` listener
    onto GTCEu's mod bus, and datagen wrote nothing.
  - Now it is `create(MOD_ID, false)`, plus `registerEventListeners(modBus)` as the first line of the `GTMQoL`
    constructor.
- 1.20.1, recurring: `MultipleArgumentsForOptionException: Found multiple arguments for option output` from the IDEA
  "Minecraft Data" config.
  - Loom puts `--all --mod --output` into `.gradle/loom-cache/launch.cfg`. An old
    `.idea/runConfigurations/Minecraft_Data.xml` that has them in `PROGRAM_PARAMETERS` too passes them twice.
  - Fix: delete the XML and re-sync, or use `.\gradlew.bat runData`.
- 1.20.1: machines must register in `GTMQoL.onRegisterMachines` (GTCEu's machine `RegisterEvent`), not in
  `IGTAddon.initializeAddon()`, which runs after the registry is frozen. On 1.21 everything is declared in the
  constructor (deferred Registrate entries), see `PORT_1_21.md`.
