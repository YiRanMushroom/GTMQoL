# GTMQoL Current Context

Handoff notes for continuing work in a new session or on another machine. Build environment, dependency
pitfalls and working rules live in `CLAUDE.md`; read that first. This file covers what exists, why it is
shaped this way, and what is pending.

## State

- Branch `migrate/gtceu-v8-runtime-generation`. Committed up to wireless covers and FE. Modular machines,
  overclocking, the Smart Assembly Factory/DTFR, the circuit/alloy tags and the RecipeDB search are staged
  but not committed yet.
- Builds, datagen and the dev client all work. Everything below has been tested in game by the user.
- Minecraft 1.20.1, Forge 47.4.1, GTCEu v8 snapshot (pinned, see `gradle.properties`), Java 17 target,
  ModDevGradle legacyForge 2.0.141 (migrated from Architectury Loom, **not built yet**, see below). Java only; no Kotlin.
- The old implementation is archived under `reference/` as research material. Do not migrate it wholesale
  and do not modify it.

## Source layout

```text
src/main/java/com/yiran/minecraft/gtmqol/
├── GTMQoL.java            entry point, GTMQoL.id(), event wiring
├── GTMQoLAddon.java       @GTAddon, owns the GTRegistrate, creative tab, machine()/multiblock() helpers,
│                          addRecipes
├── api/generation/        runtime (dynamic) resource generation, opt-in per machine
├── client/                client-only init and renders (DTFR ring)
├── common/                game content
│   ├── assembler/MagicalAssembler.java  recipe type, tiered machines, its own recipes
│   ├── circuit/           UniversalCircuits, ControlCircuits (items)
│   ├── fe/FEInputProvider.java  FE input for every GT machine and cable
│   ├── modular/           modular multiblock versions of single-block machines
│   ├── multiblock/        Smart Assembly Factory, DTFR
│   ├── overclock/         replacement OC logics
│   ├── recipedb/          non-mixin side of the grouped search
│   ├── steam/             advanced steam multiblocks, steam parallel hatch, steam magical assembler
│   ├── test/              IntegrationTests (example machines)
│   └── wireless/
│       ├── WirelessBindingTrait.java, WirelessNetworks.java, NetworkId.java, FTBTeamsCompat.java, IOStats.java
│       ├── WirelessCovers.java  cover definitions and items
│       ├── steam/         everything steam specific
│       └── energy/        everything EU specific
├── config/                GTMQoLConfig (toma, runtime toggles), EarlyConfig (gtmqol-early.properties)
├── core/                  GTMQoLMixinPlugin, RecipeDBMixinPlugin, EAPMixinPlugin
│   └── mixins/            MachineBuilder, GTMachineUtils, OverclockingLogic, GTRecipeViewerWidget,
│       │                  fusion, multi smelter, tier skipping, ManualCompression
│       ├── recipedb/      RecipeDB grouped search (own config gtmqol.recipedb.mixins.json)
│       ├── gtceufix/      workarounds for upstream bugs; delete once fixed upstream. BaseSchemaRendererMixin: MUI
│       │                  computes the multiblock preview's GL viewport from MUI's own transform only, so in
│       │                  EMI/JEI (pose-translated) it is drawn at the screen's top-left; now uses the graphics pose
│       └── eap/           ExtendedAE Plus smart doubling (own config gtmqol.eap.mixins.json)
├── data/
│   ├── recipe/            MiscRecipes, EarlyGameRecipes, WirelessRecipes (magical assembler recipes for every wireless part)
│   └── tag/               CircuitTags (GT ↔ Mekanism tags, datagen)
└── integration/           KubeJSDataGenFix; ae2/ (pattern buffer abstraction, overclocked buffer, ME machines,
                           EAP smart doubling; AE2 only)
```

Generated resources are committed under `src/generated/resources` (on the resources source set).
Static textures are under `src/main/resources/assets/gtmqol/`.

## Resources: datagen vs runtime generation

Two mechanisms exist; use datagen by default.

- **Datagen (default).** Machines registered through `GTMQoLAddon.registrate()` get blockstates, models and
  lang from Registrate when the user runs `runData`. Lang is added with `.langValue(...)` on builders and
  `registrate().addRawLang(key, value)` for everything else (see `WirelessSteamMachines.addLang`).
  After changing models or lang, the user must re-run `runData` before `runClient`.
- **Runtime generation (opt-in).** `GTMQoLAddon.machine(...)` returns `GTMQoLMachineBuilder`, which has
  `.dynamicallyGenerated(true)`. `MachineBuilderMixin` then queues the builder in `RuntimeGeneration`,
  which writes models/lang into GTCEu's `GTDynamicResourcePack` on every `RegisterDynamicResourcesEvent`.
  Used by the modular machines and the integration-test machines. Background in `docs/DYNAMIC_GENERATION.md` and
  `docs/INTEGRATION_TESTS.md` (written early on; may be partly out of date).

Datagen gotcha, already fixed: GTCEu instantiates addons while it is itself being constructed, so a
`GTRegistrate.create(MOD_ID)` in `GTMQoLAddon`'s static init hooked Registrate's `GatherDataEvent` listener
onto GTCEu's mod bus (`AbstractRegistrate.getModEventBus()` is `FMLJavaModLoadingContext.get()`), and
datagen wrote nothing. Now it is `create(MOD_ID, false)` plus `registerEventListeners(modBus)` as the
first line of the `GTMQoL` constructor.

(Loom-era note, may no longer apply after the MDG migration.) Datagen gotcha, recurring: `MultipleArgumentsForOptionException: Found multiple arguments for option
output` when running the IDEA "Minecraft Data" config. Loom puts `--all --mod --output` (from
`forge.dataGen`) into `.gradle/loom-cache/launch.cfg` under `dataArgs`; an old
`.idea/runConfigurations/Minecraft_Data.xml` that also has them in `PROGRAM_PARAMETERS` passes them twice.
Loom didn't overwrite that file on sync. Fix: delete the XML and re-sync, then check its
`PROGRAM_PARAMETERS` only has the `--existing...` args from `build.gradle`; or just use
`.\gradlew.bat runData`, which doesn't go through the XML.

Machines must register in `GTMQoL.onRegisterMachines` (GTCEu's `RegisterEvent<ResourceLocation,
MachineDefinition>`), not in `IGTAddon.initializeAddon()`, which runs after the registry is frozen.

## Config

`GTMQoLConfig` uses toma's `Configuration` library (YAML, `config/gtmqol.yaml`), not `ForgeConfigSpec`,
because values are needed during CONSTRUCT. `GTMQoLConfig.get()` registers lazily on first use (mixins in
GTCEu static initializers read it before this mod is constructed); during datagen it returns defaults
(`FMLLoader.getLaunchHandler().isData()`) so every feature gets its lang/models/tags.

Nearly all content has a toggle, default on, all restart-only: `machines.*` (smartAssemblyFactory,
dimensionallyTranscendentFusionReactor, electricImplosionCompressor, advancedSteamMachines), `modularMachines.enabled`,
`wireless.{energy,steam}`, `circuits.*`,
`recipes.{miscRecipes,earlyGame,keepManualCompression,keepVanillaTNT,netherStarDust}`, `steamTweaks.*`, `overclocking.*` (fusion and
tier skipping), `ae2.{overclockedPatternBuffer,processing,dualHatches,patternBufferReturn}`, `misc.feInput`. `integrationTests.enabled` (default
false) registers `gtmqol:runtime_single_block` and `gtmqol:runtime_multiblock`, never during datagen.
Wireless networks/binding/stats always load (shared); machine registration is gated in `GTMQoL`'s listeners.

Known limitation (user: ignore): `circuits.*` toggles don't remove the datagen'd static circuit tag JSONs.

Mixin-time options can't use it (mixin configs load before mods). They go in
`config/gtmqol-early.properties`, read by `config/EarlyConfig` with `java.util.Properties` (same approach as
GTCEu's `gtceu-early.properties`): `recipeDB.groupedSearch` (default true, `core/RecipeDBMixinPlugin`) and
`overclocking.overhaul` (default true, `core/GTMQoLMixinPlugin` skips `OverclockingLogicMixin`,
`GTRecipeViewerWidgetMixin`, `GTRecipeModifiersMixin`). Other mixins always apply and check `GTMQoLConfig` at runtime.

## Wireless steam network

A global steam pool per network, inspired by GTMThings' wireless energy.

### Networks and binding (generic, `wireless/`)

- `NetworkId(id, private)`. A team network uses the FTB team id (without FTB Teams: the player's UUID, i.e.
  their personal team). A private network uses the player's UUID. Same UUID, different pools.
- Machines store only the bound player and a private flag (`WirelessBindingTrait`, `@SaveField
  @SyncToClient`). The team is looked up on every access (`WirelessNetworks.networkId`), so joining or
  leaving a team switches networks immediately.
- `FTBTeamsCompat` is optional (compile-only + dev runtime, not in `mods.toml`).
- Binding rules:
  - Placed by a real player → bound to them, team mode (`BlockEvent.EntityPlaceEvent`, LOWEST priority,
    `FakePlayer` skipped). Anything else (Building Gadgets, fake players) stays unbound.
  - Unbound machines: anyone can bind via the UI button.
  - Unbind, toggle private, "To team": only the bound player or ops (permission level 2).
  - "To team" moves the whole private pool into the team pool and switches the machine to team mode.
- Data stick (machine implements `IDataStickInteractable`, delegates to the trait), GTMThings/GTCEu style:
  - Shift-right-click copies the binding onto the stick (bound player only), tag `GTMQoLWirelessBinding`.
  - Right-click pastes, only by that same player, onto machines that are unbound or already theirs.
  - Right-click with an empty stick binds an unbound machine to the clicker.

### Steam machines (`wireless/steam/`)

- `WirelessSteamSavedData`: the pools (`BigInteger` per `NetworkId`), on the overworld, server thread only.
  It also holds in-memory `IOStats` per network: cumulative inserted/extracted, sampled every 20 ticks
  from a server tick handler in `WirelessNetworks`, rates averaged over the last 10 samples (mB/t).
  The stats are deliberately not saved (the user agreed); `moveAll` is not counted as IO.
- `WirelessSteamTank`: a `CustomFluidTank` with no contents of its own; every read and write goes to the
  pool. It is wrapped in a normal `NotifiableFluidTank`, so GTCEu recipe/pipe/steam multiblock code works
  unchanged. Int-capped view (Forge tanks are int-sized).
- Machines (`WirelessSteamMachines`, bronze theme):
  - `wireless_steam_input_hatch` (`PartAbility.STEAM`) and `wireless_steam_output_hatch`
    (`EXPORT_FLUIDS`): `WirelessSteamHatchPartMachine`.
  - `wireless_steam_accessor`: standalone block, fluid handler on all sides, optional auto output to front.
  - `wireless_steam_monitor`: shows stored steam and input/output/net rates.
- Shared UI: `WirelessSteamUI.create(syncManager, binding, player)` shows bound player, network, stored
  steam, Bind/Unbind, Private toggle and "To team". The monitor extends it with the rates.
- Textures: both hatches use only `gtmqol:block/overlay/machine/overlay_wireless` (the animated rainbow
  swirl from GTMThings' `overlay_energy_on_wireless`, originally from GTNH) via
  `.colorOverlaySteamHullModel(WIRELESS_OVERLAY)`, with no pipe/emissive layer. As a result the input and
  output hatches look identical. The accessor uses GTCEu's `steam_hatch` model; the monitor uses GTCEu's
  screen overlay. Credits are in `README.md`.

## Wireless EU network (`wireless/energy/`)

The user chose concrete code in parallel with steam rather than a generic per-resource storage:
`WirelessEnergySavedData` (same shape as the steam one), and `FTBTeamsCompat`, `movePrivateToTeam` and
`WirelessNetworks.onServerTick` each call both. `IOStats` moved up to `wireless/` and is shared.

- Machines (`WirelessEnergyMachines`), one per tier in `GTMachineUtils.ALL_TIERS` (ULV..UHV, or ..MAX with
  GTCEu's high-tier config), names like `lv_wireless_energy_input_hatch`:
  - Input hatch: `INPUT_ENERGY`, `SUBSTATION_INPUT_ENERGY`, `INPUT_LASER`. Overclock toggle (tiers below
    MAX): one tier higher voltage, each EU costs 4 from the pool (16× energy for 4× voltage).
  - Output hatch: `OUTPUT_ENERGY`, `SUBSTATION_OUTPUT_ENERGY`, `OUTPUT_LASER`. No overclock.
  - Accessor: front emits at its tier; other sides accept any voltage (user's choice), still limited to
    its amperage per tick. Always ticking.
  - `wireless_energy_monitor`: a single machine (LV hull), EU stored and rates in EU/t.
- Amperage per machine, 1..`MAX_AMPERAGE` (2^24, user's choice), default 4 (`DEFAULT_AMPERAGE`). UI: a text
  field plus ×/÷ buttons (plain 4, Shift 16, Ctrl 2), `WirelessEnergyUI.amperageRow`.
- Per-player default: `/gtmqol default_amperage [amperage]` (`DefaultAmperageCommand`, any player, for
  themselves), saved in `WirelessEnergySavedData`. Applied in `WirelessNetworks.onEntityPlace` to hatches
  and accessors the player places; other placers get 4.
- The hatch Save button is ModularUI's `GuiTextures.SAVE` (floppy) icon, label in the tooltip.
- `WirelessEnergyContainer` (hatches) reports a **per-tick budget** of V × A as stored/capacity, not the
  pool: the power substation drains `getEnergyStored()` every tick, and `EnergyContainerList` sums all
  hatches, so reporting the pool would be counted once per hatch.
- Hatch settings (amperage, overclock) are edited in the UI and applied by the Save button or on UI close
  (`applySettings`). Multiblocks snapshot voltage/amperage/tier when they form, so applying re-forms every
  formed controller the GTCEu way (`checkStructurePattern` hits the cache, then `formStructure`), same as
  `PatternState.onBlockStateChanged`. User approved. The accessor's amperage applies immediately.
- `WirelessEnergyContainer` overrides `getTotalContentAmount()` to return `getEnergyStored()`: the base
  returns its raw `energyStored` field (always 0 here), and `RecipeHandlerList.handleRecipe` skips input
  handlers whose total is 0, so without it recipes report insufficient inputs.
- Both wireless containers (hatch and accessor) override `getContents()` to return `new EnergyStack(stored)`.
  The base goes through `EnergyContainerList.calculateVoltageAmperage`, whose `hasPrimeFactorGreaterThanTwo`
  is linear in the amperage (powers of two are the worst). At our 2^24 A that is millions of iterations per
  call, on every parallel calculation (found on 1.21, ~77% of a server profile; same GTCEu code here). The one
  call in `EnergyContainerList`'s constructor (on form) is left as is.
- Known limitation: GTCEu sums hatches in longs; one MAX hatch at full amperage is 2^55 EU/t, so it takes
  about 256 such hatches on one multiblock to overflow.
- The EU UI reuses the steam binding lang keys (registered in `WirelessSteamMachines`).

## Wireless covers (`wireless/WirelessCovers.java`)

- Registered from GTCEu's `RegisterEvent<ResourceLocation, CoverDefinition>` (`GTMQoL.onRegisterCovers`;
  `IGTAddon.registerCovers` is deprecated). Cover items are made in the same call through our registrate.
- `WirelessEnergyCover`: input and output, one per tier (`GTMachineUtils.ALL_TIERS`), ids like
  `gtmqol:wireless_energy_input.lv`, items `lv_wireless_energy_input_cover`. Moves up to V × A EU/t with
  `changeEnergy` straight into / out of the machine's buffer, so the machine's voltage doesn't matter and
  nothing explodes. Input needs `getInputVoltage() > 0`, output `getOutputVoltage() > 0` to attach.
  Amperage UI and per-player default as for the hatches.
- `WirelessSteamCover`: input fills the machine's fluid handler with network steam, output drains steam
  (boilers) into the network, as much as the machine takes/gives per tick.
- Binding: the placing player (`onAttached`). Covers have no machine, so they hold a
  `WirelessBindingTrait` built with the `(ISyncManaged owner, BooleanSupplier isRemote)` constructor as a
  sync field (like GTCEu's `FilterHandler`); `MachineTrait` methods needing a machine would throw. No data
  stick support on covers.
- All four use the rainbow `overlay_wireless` texture for the cover and the item, so they look the same.

## EU ↔ FE (`fe/FEInputProvider.java`)

- The user wanted every EU input to accept FE and every EU output to give FE.
- FE → EU: an `AttachCapabilitiesEvent<BlockEntity>` provider on every `MetaMachine` and `CableBlockEntity`
  exposes `ForgeCapabilities.ENERGY` on sides where the machine/cable takes EU, converting at GTCEu's
  `feToEuRatio`. Packets are at most the input voltage, so nothing explodes. It looks the container up
  without attached caps (`MetaMachine.getCapability(machine, ...)`, `cable.getEnergyContainer`), otherwise
  it and GTCEu's `EUToFEProvider` would query each other forever.
- EU → FE: nothing to do; GTCEu's `EUToFEProvider` (`nativeEUToFE`, default on) gives FE blocks an EU
  capability, so outputs and cables push into them. Pull-based FE pipes can't extract from GT machines.
- No config toggle (the old implementation had `enableFEToEUConversion`).

## Recipes, magical assembler, circuits

- Recipes go through `IGTAddon.addRecipes` (`GTMQoLAddon`), which GTCEu runs into its runtime data pack
  together with its own recipes, so no recipe JSON is datagen'd.
- `MagicalAssembler`: recipe type `gtmqol:magical_assembler` (16/1 items, 4/1 fluids, default `VA[LV]`),
  registered from GTCEu's `RegisterEvent<…, GTRecipeType>`; machines via GTCEu's `SimpleMachineBuilder`
  (electric tiers), textures copied from `reference/`. Ported recipes from the old `QoLRecipes.kt`: the
  machine's crafting recipe (`PGP/GMG/PCP`), circuit → universal circuit (circuit 5, 1 tick, 1 EU/t), produce
  and copy creative data access hatch. The Smart Assembly Factory and DTFR recipes live in
  `GTMQoLMultiblocks.addRecipes`. Not ported (outputs don't exist any more): probable (im)probability
  devices, industrial LCR.
- `UniversalCircuits`: `<tier>_universal_circuit` for every `GTValues.ALL_TIERS` tier, tagged
  `gtceu:circuits/<tier>`, old textures.
- `CircuitTags` (datagen, item tags): `forge:circuits/{basic,advanced,elite,ultimate}` includes
  `#gtceu:circuits/{lv,mv,hv,ev}`, and each GT tag includes the matching `mekanism:*_control_circuit` as an
  optional entry. Not mutual tag references, which would be a cycle. The old version rebound holder sets
  at `TagsUpdatedEvent` and had a config toggle; static tags can't be toggled, so there is none.
  ULV pairs with infused alloy the same way: `forge:alloys/advanced` includes `#gtceu:circuits/ulv`
  (Mekanism recipes use both `forge:alloys/advanced` and `mekanism:alloys/infused`, the latter includes the
  former), and `gtceu:circuits/ulv` includes `mekanism:alloy_infused`.
- `MaterialAliasTags` (datagen, item/block/fluid tags, always on): GT spells some materials differently from other
  mods. For each of the material's `MaterialEntry` (`ItemMaterialData.MATERIAL_ENTRY_ITEM_MAP` / `_BLOCK_MAP`) and
  each non-parent `TagPrefix` tag whose path contains the name, the alias tag includes `#<GT tag>` as an optional tag
  (e.g. `forge:ingots/aluminum` → `#forge:ingots/aluminium`, `forge:plutonium` fluid → `#forge:plutonium_239`).
  Optional because GT's material tags are generated at runtime. Aliases: aluminium → aluminum, plutonium_239 → plutonium.
- `MagicalAssemblerUI`: v8 recipe UI is ModularUI, configured through `GTRecipeType.UI(GTRecipeTypeUILayout.Builder)`.
  Grids can be changed with `setLayoutGridBuilder` (`String[]`, `'s'` = slot), but every capability is
  stacked vertically in `inputColumn`, so putting fluids *beside* items needs custom per-capability builders
  (`setMachineCapabilityLayoutBuilder` / `setRecipeViewerLayoutCapabilityLayoutBuilder`): the item builder
  puts a 4×4 item grid and a 1×4 fluid column in one row, the fluid builder skips IN; outputs use the defaults.
- Mekanism: dev runtime only (`modLocalRuntime`, modmaven), nothing compiled against it.
- `WirelessRecipes` (magical assembler, LV): GT energy input/output hatch + circuit 5 → wireless hatch;
  wireless hatch + circuit 5 → 4 covers of the same direction; GT output hatch + 4 input covers →
  accessor; screen cover + LV input cover → monitor. Steam is the same from GT's `STEAM_HATCH` (output
  hatch uses circuit 6, as GT has no steam output hatch).

## Modular machines (`modular/`)

- `MachineBuilderMixin` (RETURN of `MachineBuilder.register()`) hands every non-multiblock machine to
  `ModularMachines`. The user's rule (2026-10-08): for each set of recipe types, the first machine registered
  with tier ≥ LV gets the modular machine (steam machines share the types but are tier 0, so the LV one wins).
  Only machines named `VN[tier].toLowerCase()+"_"<name>` count: GTCEu (and addons copying it) register `hp_` steam
  machines with `tier(1)` = LV, and before this check they were picked as "the LV machine" for their recipe types.
  It is named after that machine with the prefix stripped; if that name is taken by
  a different recipe type set, a warning is logged and it gets none. `registerTieredMachines`, KubeJS tiered
  machines and addons looping their tiers themselves all end in `register()`. Replaced master's approach (RETURN
  of `registerTieredMachines` + `KJSTieredMachineBuilder` mixin), which missed self-looping machines.
  On 1.20 it registers right away from the definition (`getTier()`, `getRecipeTypes()`), nested inside the
  single block's `register()` in the machine event. `ModularMachines.register`
  then registers `gtmqol:modular_<name>` (other addons: `modular_<ns>_<name>`) through our registrate, with
  `dynamicallyGenerated(true)` (models and en_us lang at runtime, nothing datagen'd; skipped during datagen).
- Port of the old `AddModularMultiblocksLogic.kt`: skip machines without recipe types, with `DUMMY_RECIPES`, or
  mixing generator and non-generator types. 3×3×3, controller at front centre, any block allowed (casing/glass
  preview, auto abilities; no parallel hatch, the user only wants subtick parallels). Modifiers: duration ×0.125 (generators ×8), then `OC_PERFECT_SUBTICK`
  (generators `create(0.5, 4.0, true)`), then `BATCH_MODE`. Overlay: the single block's
  `block/{machines,generators}/<name>` if it has `overlay_front.png`, else implosion compressor / large
  combustion engine. Recipes: magical assembler (circuit 5) and hammer shaped, from the first tier.
- `ModularMachine.getMaxVoltage()` returns `getOverclockVoltage()` (the old
  `SingleHatchTierSkippingWorkableElectricMachine`).
- Config `modularMachines.enabled` (default true). Not ported: the
  `QOL_RECIPE_MODIFIER` part ability, non-English names.

## Overclocking (`overclock/`, `OverclockingLogicMixin`)

- The factor constants (`STD_VOLTAGE_FACTOR` …) are interface fields, so implicitly `static final`
  compile-time constants inlined by javac; patching them does nothing. The four logic constants are objects
  but `static final` too (the old `OverclockingPatcher` replaced them with Unsafe). Instead,
  `OverclockingLogicMixin` (interface mixin, `@Overwrite` only — the old project's approach) overwrites:
  - `getModifier`: `Overclocking.replace` maps `NON_PERFECT_OVERCLOCK(_SUBTICK)` → 4× EU/t 4× speed and
    `PERFECT_OVERCLOCK(_SUBTICK)` → 2× EU/t 4× speed, both subtick; always computes parallels
    (`getParallelAmountWithoutEU`); no ULV OC penalty (as in the old mixin).
  - `subTickParallelOC`: equivalent OC (fractional OC levels, speed rounded down to whole parallels once at
    1 tick). Affects every subtick logic, including generators' `create(0.5, 4.0, true)`.
  - `heatingCoilOC`: every OC 4× EU/t for 8× speed, coil temperature ignored (old behaviour).
- `GTRecipeViewerWidgetMixin` applies the same replacement to the recipe viewer's OC preview, which calls
  `runOverclockingLogic` directly.
- `GTRecipeModifiersMixin` (always on, no toggle): with the new OC computing parallels, the multi smelter's
  base → OC → parallel order gives wrong results, so the middle OC of `multiSmelterParallel` becomes identity
  and the OC is appended to the returned function, computed on the parallelized recipe.
- No config toggle for the above (overwrites can't be switched off at runtime). The following have toggles
  under `overclocking.*` (all default true, restart):
  - `buffFusionReactor`: `FusionReactorMachineMixin` wraps `FUSION_OC = create(...)` in `<clinit>` to return
    `PERFECT_OVERCLOCK_SUBTICK`, and the `getModifier` calls in `recipeModifier` to pass
    `getOverclockVoltage()` with parallels instead of the tier-capped `getMaxVoltage()`. `GTMultiMachinesMixin`
    adds substation and laser hatches to the fusion 'E' slot by wrapping `PartAbility.getBlockRange(II)`
    (its only call in `GTMultiMachines`) inside a regex-selected lambda (`/^lambda\$/`, works).
  - `enableMultiTierSkipping`: `WorkableElectricMultiblockMachineMixin` wraps `return V[...]` in
    `getMaxVoltage` (the "several hatches at the highest tier → tier + 1" branch) to return the summed hatch
    voltage (`VEX[floor tier]` when amperage is 1).

## Smart Assembly Factory and DTFR (`multiblock/`)

Ports of the old ones (the user called the former "精度装配机"; assumed to be the Smart Assembly Factory).
Registered in `onRegisterMachines`, lang/models datagen'd, recipes in the magical assembler.

- Smart Assembly Factory: assembly line recipes, `PARALLEL_HATCH`, `OC_PERFECT_SUBTICK`, `BATCH_MODE`.
  Pattern from 7.x `start(BACK, UP, RIGHT)` → v8 `start(RIGHT, UP, BACK)`, `setRepeatable(4)` →
  `sliceRepeatable(4, 4, ...)`. Data hatch via `.and(dataHatchPredicate())` (null when research is off).
- DTFR: fusion recipes of any tier (ignores `eu_to_start`), same modifiers. `DTFRMachine` is a plain
  `WorkableElectricMultiblockMachine` (no fusion buffer/heat/tier cap) holding the ring fade state, because
  the dynamic render instance is shared. Fusion MK3 layout with all fluid slots as 'X'; energy slots also
  accept substation/laser hatches (new vs 7.x). Ring: `client/DTFRRingRender` (gtceu's `FusionRingRender`
  without bloom, white), registered as `gtmqol:dtfr_ring` in `GTMQoLClient.init()` from the constructor
  on the client dist.

## Nether stars, electric implosion compressor (`implosion/ElectricImplosion`)

Same as 1.21 (see its context.md), goal: nether stars before IV.
- `recipes.keepVanillaTNT`: `RecipeRemovalMixin` `@ModifyExpressionValue`s the `removeVanillaTNTRecipe` read in
  `RecipeRemoval.generalRemovals` to false.
- `recipes.netherStarDust`: mixer, 4 diamond dust + 16 silver dust → 1 nether star dust, 20 s, `VA[HV]`.
- `machines.electricImplosionCompressor`: recipe type (recipe type `RegisterEvent`) and multiblock (machine
  `RegisterEvent`) `gtmqol:electric_implosion_compressor`. Only the vanilla TNT implosion variant is copied, as
  `implode_<x>_electric` without the TNT, 4× duration. Hooked with `IMPLOSION_RECIPES.onRecipeBuild` when our
  recipe type registers; 1.20's `GTRecipeType` has no getter for the prototype, so the previous `onSave` is read
  from a `recipeBuilder(...)` copy and called first. Unlike 1.21, 1.20 GTCEu has no `RecipeManagerLateMixin`
  regeneration, so KubeJS / data pack implosion recipes get no copy.
- Not yet in game.

## Cyclic multiblocks, void miner, fishing pond (`multiblock/`)

Ported from 1.21 on 2026-10-07 (see its context.md for the behavior), written, not built yet. Files:
`CyclicMultiblockMachine`, `PendingOutputTrait`, `VoidMinerMachine`, `VoidMinerOres`, `FishingPondMachine`,
`client/FishingPondWaterRender` (registered in `GTMQoLClient`), definitions/lang/recipes in `GTMQoLMultiblocks`,
config `machines.voidMiner`, `machines.fishingPond`, `voidMiner.dimensionMapping`. Needs `runData` for models/lang.
Port differences:
- UI sync: ModularUI 1.20's `GenericListSyncHandler.Builder<T>` takes `(FriendlyByteBuf, T)` / `FriendlyByteBuf -> T`,
  so `Stored` / `OreChance` have static `read`/`write` instead of `StreamCodec`s. `Mth.clamp` (no Java 21 `Math.clamp`).
- `PendingOutputTrait` merges by `isSameItemSameTags`.
- Ores: `GTRegistries.ORE_VEINS` (not a datapack registry here); `ChemicalHelper.getItem` may return null.
- Fishing: `ToolActions.FISHING_ROD_CAST`, Lure = `getFishingSpeedBonus(tool) * 5 s`,
  `getFishingLuckBonus(tool)`, `server.getLootData().getLootTable`. `new FishingHook(EntityType, Level)`
  visibility not checked yet.
- Same `startOffset` fishing pond pattern as 1.21.

## Crystal Growth Chamber (`crystal/CrystalGrowth`, `crystal/CenterBlockCondition`)

Ported from 1.21 on 2026-10-07 (it works there; see its context.md for the design), written, not built yet.
Config `machines.crystalGrowthChamber`. Port differences:
- The condition's `CODEC` is a `Codec` (`RecordCodecBuilder.create`), not a `MapCodec`.
- `gtmqol:center_block` is registered from GTCEu's `RegisterEvent` for `RecipeConditionType` (posted by
  `GTRecipeConditions.init()`, same `CommonProxy.init()` as recipe types and machines), listener in `GTMQoL`.
- Recipe type via `GTRecipeTypes.register`, machine from the machine `RegisterEvent`, like the electric implosion
  compressor. GeOre's 1.20 branch uses the same `budding_<x>` / `<x>_shard` names.

## Greenhouse (`greenhouse/Greenhouse`, `integration/mysticalagriculture/MAGreenhouseRecipes`)

Ported from 1.21 on 2026-10-07 together with it (see its context.md for the design), built on neither yet.
Config `machines.greenhouse`. Tiered single-block Greenhouses (every electric tier) plus the IV Industrial Greenhouse
multiblock (5 × 5 × 5, dirt-tag block in the middle of the floor, 16× outputs via its own `RecipeModifier`,
parallel + perfect OC), sharing recipe type `gtmqol:greenhouse`. Recipes are `VA[LV]`.
Port differences:
- Recipe type via `GTRecipeTypes.register` (`initRecipeType`), both machines from the machine `RegisterEvent`
  (`initMachines`), recipes on `Consumer<FinishedRecipe>`, `MachineDefinition[]` for `registerMachineRecipe`.
- Mystical Agriculture 1.20.1-7.0.24 + Cucumber from Modrinth (`modCompileOnly` / `modLocalRuntime`). Its 1.20
  API has no `MysticalAgricultureAPI.resource`, so the fertilized essence id is built by hand.

## Advanced steam multiblocks (`steam/`, `circuit/ControlCircuits`)

Written, not built or tested yet. Modelled on GTNL's steam multis (GTNH addon, LGPL-3.0, credited in README;
reference clone at `E:\code\Minecraft\gtnl-ref`, outside the repo), not a copy.

- `AdvancedSteamMultiMachine extends SteamParallelMultiblockMachine` (the user's "advanced_steam_multi"),
  `maxRecipeTier` per machine (default MV). `recipeModifier`: reject above the tier; non-perfect OC (×4 EU/t,
  ÷2 duration) from `max(tier, LV)` up to it (ULV one OC less, as GTCEu), stops at 1 tick; then duration ×0.8,
  EU/t ×0.75 (steam = EU × 2 mB, from the superclass). Parallels: 16, or the `SteamParallelHatchPartMachine`
  found in `formStructure` (16–256, default 256, GTCEu parallel hatch UI with ÷2/×2, own `PartAbility`
  `steam_parallel_hatch`, max one, not shareable).
- `AdvancedSteamMachines`: `gtmqol:large_steam_<x>` for 21 recipe types (circuit assembler MV so it makes the
  ULV–HV control circuits; magical assembler LV as the user asked). All bronze (user: GTCEu steam multis
  have no steel tier), bronze theme, bronze plated bricks appearance, GTCEu's single-block overlay.
  Hatch casing `X`: steam buses + `autoAbilities` buses/hatches of any tier, one steam hatch, at most one
  parallel hatch, at least half of the `X` positions must be casings.
  Also `lp_/hp_steam_magical_assembler` (`registerSteamMachines` + `SimpleSteamMachine`, our magical
  assembler overlay; HP keeps GTCEu's steel theme like other HP single blocks). Recipes: LP steam magical
  assembler shaped (bronze hull, crafting tables); HP like GTCEu's steel upgrades; each multi in the magical
  assembler (1 LP steam single block + 2 bronze gearbox + 4 bronze bricks + 8 bronze plates + 2 bronze gears,
  no circuits, ULV EU/t, so the steam one can make them); parallel hatch from a steam hatch + 2 bronze
  gearboxes + 4 LV circuits (user: no LV machines or circuits in the large steam multis).
  The 14 bases GTCEu has no steam version of (bender … circuit assembler) get `gtmqol:lp_/hp_steam_<x>` via
  `GTMachineUtils.registerSimpleSteamMachines` (GTCEu's overlay). LP: `PCP/XMX/PXP` (bronze plates, small bronze
  pipes, bronze hull, `C` a per-machine key item; the circuit assembler's key is 1 LV circuit, the only
  circuit, as the user asked); HP: GTCEu's steel upgrade layout `WSW/PMP/WWW`.
- Steam single blocks have full IO: `SimpleSteamMachineMixin` also attaches persistent fluid tanks sized by the
  recipe type (8 B, like LV). The input tank refuses steam (`setFilter`) and is attached with trait priority 2,
  above the steam tank (also an IN `NotifiableFluidTank`): GTCEu's machine UI uses the first IN fluid handler
  as the recipe slots and `getTraits` is sorted by priority, otherwise it drew the steam tank (and a 2-input
  mixer would index past its one slot). `GTMachinesMixin` / `GTMultiMachinesMixin` drop every
  `addOutputLimit` in GTCEu's registration (steam macerator, LV–HV macerators, steam grinder, steam oven;
  user wants byproducts everywhere). Generator output limits (in `GTMachineUtils`) are untouched.
- `AdvancedSteamShapes`: generated once by a Python script from GTNL's `assets/sciencenotleisure/multiblock/
  large_steam_*.mbs` (format: `MBS1`, string table of int length + UTF-8, then rows of int indices;
  `shape[row top→bottom][slice front→back]`, strings along X, `~` controller, space any). Rows reversed to
  bottom-up for `MultiblockPatternBuilder.start()` (BACK, UP, RIGHT). Every GTNL letter is remapped to one
  shared symbol set (documented on the class): GT5 bronze casings → GTCEu bronze casings, GTNL/GT++ decorative
  casings → bronze hull / bronze brick hull / plated bricks, potin → bronze block, glass → `forge:glass`.
  The assembler and magical assembler have no GTNL large steam shape and use `steam_manufacturer`. Possibly
  mirrored relative to GTNL (char direction), harmless.
- `ControlCircuits`: GTNL's MetaItem 21–25 (very simple … elite, ULV–EV), textures from GTNL, tagged
  `gtceu:circuits/<tier>`. Recipes ported from GTNL's `CircuitAssemblerRecipes` with GTCEu substitutes
  (cast iron → wrought iron; GT5 circuits → NAND chip / electronic / integrated circuits as not-consumed).
  Names overlap Mekanism's basic/advanced/elite control circuits (display only, different ids).
  The whole project is LGPL-3.0 like GTNL/GTNH (`LICENSE`, text copied from GTNL, also packed into the jar;
  authors "Yiran, Frosty").
- `MagicalAssemblerUI` builds the 4-slot fluid column from the first IN fluid handler. On a steam machine that
  handler is `SteamMachine.steamTank` (1 slot), so indexing slots 1–3 threw and the UI did not open on right
  click; the column is now skipped when the handler is the steam tank. With the mixin's input tank first, the
  steam magical assembler now shows and uses the fluid column; the steam tank check is only a fallback.
- Ghost circuit on steam: `SimpleSteamMachineMixin` attaches `ProgrammableCircuitSlotTrait` (as "circuit", like
  `SimpleTieredMachine`) to every steam single block, GTCEu's included; `GTSingleblockMachinePanelsMixin` turns
  trait configurators back on for the steam panel (`defaultSteamMachinePanelBuilder` disables them) so the slot
  shows; `SteamItemBusPartMachineMixin` re-enables the bus circuit slot GTCEu turns off (input bus only), which
  gives steam multis ghost circuits through their input bus. The constructor inject must be `<init>*`: plain
  `<init>` only matched one of `SimpleSteamMachine`'s two constructors.
- Early game (steam before LV): `GTMachineUtilsMixin` makes the large boiler's muffler and maintenance hatch
  optional (the two `setExactLimit` in the `registerLargeBoiler` pattern lambda → `setMaxGlobalLimited`,
  `require = 2`). `steam/EarlyGameRecipes`: ULV/LV machine casing + chest = bus, + glass = hatch, chest/glass on
  top = input, below = output. `RecipeRemovalMixin` keeps `minecraft:glass` (sand smelting) out of gtceu's
  `hardGlassRecipes` removals. Magical assembler: any sapling + sticky resin → rubber sapling.
  `ManualCompressionMixin` (`recipes.keepManualCompression`) wraps every read of
  `ConfigHolder$RecipeConfigs.disableManualCompression` (`RecipeAddition`, `RecipeRemoval.init`,
  `MaterialRecipeHandler.processNugget/processBlock`, `OreRecipeHandler.processRawOre`) so 3x3/2x2
  block/ingot/nugget/raw ore crafting stays. Setting the field from the constructor (the old approach) didn't stick.

## RecipeDB grouped search (`recipedb/`, `mixin/recipedb/`)

Port of the old `RecipeDBMixin` (late-game lag fix). Full write-up, including what the 7.x patches did and
why most of them are obsolete in v8, in `docs/RECIPEDB_REFACTOR.md`. In short:

- `fromHolder` is overwritten to tag each ingredient entry with its `RecipeHandlerList` group.
- Pattern buffers are split per worker, with the circuit and shared inventories as catalysts.
- `RecipeIterator`'s branch pushes are filtered to entries one group could supply together.
- The whole set is gated by `core/RecipeDBMixinPlugin` (`recipeDB.groupedSearch` in
  `config/gtmqol-early.properties`, default true).
- `lambda$getNext$0` (javac's default name) is the right target. AE2 is `modCompileOnly` because
  `PatternBufferIngredients` touches GTCEu's pattern buffer, which implements AE2 interfaces.

## AE2: pattern buffers and smart doubling (`ae2/`)

Written, not built or tested yet. Everything here only runs when `GTCEu.Mods.isAE2Loaded()`.

- `AbstractMEPatternBufferPartMachine extends MEPatternBufferPartMachine`: `getPatternColumns()` /
  `getPatternRows()` (must return constants, they are called from the superclass constructor).
  GTCEu's `MAX_PATTERN_COUNT = 27` is inlined; `mixin/MEPatternBufferPartMachineMixin` `@ModifyConstant`s the
  five 27s in `<init>` (pattern inventory, internal slots, pattern details), `syncWorkerCount`, `addWorker`
  (`require = 5`, recheck after a GTCEu bump). `getTerminalPatternInventory` and `buildMainUI` (scrolls past
  6 rows) are overridden instead. The shared inventory/tank stay GTCEu's 9 slots (the old 25-slot catalyst
  version would need the whole `getPanelBuilder`). Unformed, the pattern terminal shows GTCEu's buffer icon
  (`getTerminalGroup`, `customName` is private).
- `AE2Machines`: `gtmqol:overclocked_me_pattern_buffer`, 12 columns × 18 rows = 216, LuV; recipe in the
  magical assembler (4 ME pattern buffers, 16 MV circuits, circuit 24, 576 soldering alloy, 4000 glue, 1200 t,
  MV), as in the old `QoLMachines.kt`.
- `AEProcessing` (port of the v7 ME machines, AE2 only): recipe types and SimpleTieredMachines
  `gtmqol:me_assembler` (6/1/3/0) and `gtmqol:me_circuit_slicer` (1/1/0/0), the four silicon chip items, and the
  v7 recipes: machine crafting (AE2 inscriber in the middle), wafer → chips (8/16/32/64), AE2 materials → prints,
  chip + print + silicon print (or 4 copper foil) + 144 redstone → processors ×chip multiplier, GTCEu ME
  buses/hatches/pattern buffer (+proxy) from AE2 parts, and wiremill/polarizer/mixer AE recipes. Items in the
  constructor, recipe types / machines from the GTCEu register events (the sticky card is below). Machine names come from `SimpleMachineBuilder`'s
  `toEnglishName`, so they read "Me Assembler".
- AE addon recipes ported from 1.21 (2026-10-07, not built or tested): slicer prints / assembler processors /
  mixer, macerator, polarizer, compressor recipes for Advanced AE (`advanced_ae`), MEGA Cells (`megacells`) and
  Applied Flux (`appflux`), plus the oblivion singularity implosion for ExtendedAE Plus, each behind
  `GTCEu.isModLoaded` and `!= Items.AIR` checks (an id that differs just skips its recipe silently). The
  ExtendedAE entro / concurrent processor and EAP lattra recipes are left out: ExtendedAE 1.20 (mod id
  `expatternprovider`, 1.4.18 jar checked) has no entro items or concurrent processor, and EAP 1.6.1 has
  `lattra_crystal` / `oblivion_singularity` but no `lattra_dust`. AdvancedAE / MEGA / AppFlux ids not checked
  against 1.20 jars. `c:` tags became `forge:` (`forge:silicone` for the insulating resin is a guess).
- Sticky card and universal circuit encoding, ported from the v7 code in `reference/` (same as the 1.21 branch;
  written, not built or tested). `gtmqol.ae2.mixins.json` (in `build.gradle` `mixinConfig`), gated by
  `core/AE2MixinPlugin`: AE2 loaded; the ExtendedAE mixin also needs `expatternprovider` (ExtendedAE's 1.20.1 mod
  id). Two early switches in `config/gtmqol-early.properties` (`EarlyConfig`): `ae2.universalCircuitEncoding` (only
  `EncodingHelperMixin`, on `appeng.integration.modules.jeirei.EncodingHelper`) and `ae2.stickyCard` (the other
  four mixins in `core/mixins/ae2`, the item `integration/ae2/StickyCardItem` and its recipes). Mixins as on 1.21:
  `MEInventoryHandlerMixin` adds `ISticky`, `NetworkStorageMixin` stops the insert loops with a `@Share` flag,
  `StorageBusPartMixin` (`@Shadow` on the private `handler`: AT `META-INF/accesstransformer.cfg` + ModAccessor `accessModCompileOnly` for AE2, after the MDG migration; a `@WrapOperation` with the supertype receiver did not match) and `PartSpecialStorageBusMixin` set the flag. The Upgrades have no tooltip group
  (v7 passed `group.storage.name`; AE2's own storage bus upgrades have none). ExtendedAE is now `modCompileOnly`
  too (`EPPItemAndBlock.*_STORAGE_BUS`).
- Pattern encoding skips non-consumed inputs (toma `ae2.skipNotConsumedInputs`, runtime, default on), client
  mixins applied only when the viewer is loaded; AE2 skips empty input lists:
  - EMI: `EmiEncodePatternHandlerMixin` wraps `EmiStackHelper.ofInputs`, blanks `EmiIngredient.getChance() == 0`.
  - JEI: `EncodePatternTransferHandlerMixin` wraps `GenericEntryStackHelper.ofInputs`. JEI slots have no chance, so
    for a `GTRecipe` it rebuilds `GTRecipeJEICategory`'s layout (one INPUT slot per stack of
    `JeiIngredientHandler.toJeiIngredient(mapped)`, items then fluids) and blanks slots of chance-0 contents; if the
    slot count doesn't match it leaves the inputs alone. JEI is `modCompileOnly` for this.
- Smart doubling (ExtendedAE Plus, optional): `gtmqol.eap.mixins.json`, gated by `core/EAPMixinPlugin`
  (`LoadingModList` has `extendedae_plus`). `mixin/eap/MEPatternBufferSmartDoublingMixin` on GTCEu's buffer
  (so ours too) implements `ISmartDoublingHolder`, `@SaveField` toggle (default on) and limit (0 = none),
  copies them onto the slot patterns (`ISmartDoublingAwarePattern`, `PatternScaler.getComputedMul`) at
  `getAvailablePatterns` HEAD and in the setters. `pushPattern` unwraps `ScaledProcessingPattern` to its
  original for the slot/worker matching (and `worker.pattern`, so refunds still match) but pushes the scaled
  one's inputs (`@ModifyArg` on both `InternalSlot.pushPattern`). The user said this unwrap is required.
  UI: a "×2" left configurator opening a popup (toggle + limit), added by `@ModifyReturnValue` on
  `getPanelBuilder` (`SmartDoubling.addConfigurator`). Lang keys registered in `AE2Machines`.
- ME dual parts and pattern buffer return, ported from 1.21 (`AEDualParts`, `MEDualInputPartMachine`,
  `MEDualOutputPartMachine`, `FluidConfigWidget`, `PatternBufferReturn`, `MEPatternBufferReturnMixin`; written, not
  built or tested). Same behavior and config keys (`ae2.dualHatches`, `ae2.patternBufferReturn`) as 1.21, see its
  context. Port differences: forge packages, `FluidStack.readFromPacket/writeToPacket`, config tag methods without
  `HolderLookup.Provider`, GTCEu's mutable `Ingredient`/`SizedIngredient`/`FluidIngredient` (the unfinished rest is
  written back with `setAmount`/`setCount` instead of replacing the list entry), `MachineDefinition` /
  `GTRecipeType` / `Consumer<FinishedRecipe>`, and `PatternBufferReturn` builds its own `IActionSource` because the
  buffer's `actionSource` is protected. The fluid list uses `attachPersistentTrait`.
  The 1.21 `AEConfigSyncHandlerMixin` is not ported: ModularUI 1.20.1's `syncToClient` writes eagerly, so the bug
  probably doesn't exist here; port it if a stocking bus's config UI doesn't update while open.
- Versions are constrained by GTCEu's JEI mixins: JEI stays 15.20.0.115, so EAP stays 1.6.1 (see
  `gradle.properties`).
- Known, ignored for now: a JVM access violation (C2 JIT, `InventoryChangeTrigger`) once while picking up a
  buffer.

## Pending / open

- Known, not fixing (GTCEu issue, 2026-10-06): on steam (BRONZE theme) machine panels, including our wireless steam ones, 
  the title and the GT logo are pushed to the right and look squeezed. GTCEu's own steam machines do the same, 
  so it is upstream; revisit only if GTCEu changes the panel.
- Open question: should the accessor also use the rainbow overlay?
- Known bug, not fixed (user: leave it for now; workaround: don't reload client resources, restart if hit):
  after a client resource reload (F3+T, resource pack / language / mipmap change) every GTCEu bronze/steel themed
  UI (steam single blocks, steam generators) fails to open with `ClassCastException: IDrawable$2 (NONE) cannot
  be cast to UITexture` in GTCEu `MachineUIPanel.<init>`. Seen on 1.21.1 in a pack (2026-10-02); dev only
  loads themes once, so it looks fine. Cause is ModularUI: `ThemeManager` merges the java-registered theme into
  a new `JsonBuilder` with `addAllOf` (shallow), then `parse` writes `background: "none"` (hover marker, when
  `panel` has no hover theme) into the shared `panel` object, i.e. into GTCEu's registered `GTGuiTheme` JSON.
  The next reload reads `"none"` as the panel background. Not checked whether 1.20.1's MUI build has the same
  code. Possible fix: client mixin in `gtceufix/` wrapping that `jsonBuilder.addAllOf(builder)` to pass
  `builder.getJson().deepCopy()`.
- Done: jar naming is `gtmqol-<mod version>-<mc version>.jar` (currently `gtmqol-2.0.0-1.20.1.jar`).
  `archives_base_name=gtmqol`; `build.gradle` sets `archiveVersion` on every `AbstractArchiveTask`
  (including `reobfJar`). `project.version` stays `mod_version` (2.0.0), which is what goes into
  `mods.toml`. More naming details to be added later.

## User preferences not covered by `CLAUDE.md`

- `README.md` is entirely in English. Credits for borrowed ideas and assets go in its Credits section,
  never in code comments.
- The user replies in Chinese.
- The user runs all Gradle tasks (build, `runData`, `runClient`) and pastes logs back; tell them exactly
  what to look for in game.

## Build migration: Loom -> ModDevGradle legacyForge (untested)

`build.gradle` / `settings.gradle` follow gtceu's 1.20.1 branch: plugin `net.neoforged.moddev.legacyforge`,
`legacyForge {}` (version, parchment, runs, mods), `mixin {}` (refmap `gtmqol.refmap.json` + the four configs),
`obfuscation { createRemappingConfiguration(configurations.localRuntime) }` for `modLocalRuntime`, MixinExtras
as `jarJar`, ModAccessor for the AT. Things to check on the first build: `nameSyntheticMembers` has no switch
here (Jade `this$0` mixin), whether the full gtceu jar's jarjar now works (we still use `:slim` plus explicit
deps), `EvalEx` as plain `runtimeOnly`, run configs / datagen args, CLAUDE.md's 1.20 section is still Loom.
