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
  Architectury Loom 1.13. Java only; no Kotlin.
- The old implementation is archived under `reference/` as research material. Do not migrate it wholesale
  and do not modify it.

## Source layout

```text
src/main/java/com/yiran/minecraft/gtmqol/
├── GTMQoL.java            entry point, GTMQoL.id(), event wiring
├── GTMQoLAddon.java       @GTAddon, owns the GTRegistrate, creative tab, machine()/multiblock() helpers,
│                          addRecipes
├── ae2/                   pattern buffer abstraction, overclocked buffer, EAP smart doubling (AE2 only)
├── assembler/MagicalAssembler.java  recipe type, tiered machines, its own recipes
├── circuit/               UniversalCircuits, ControlCircuits (items), CircuitTags (GT ↔ Mekanism tags, datagen)
├── client/                client-only init and renders (DTFR ring)
├── config/GTMQoLConfig.java
├── fe/FEInputProvider.java  FE input for every GT machine and cable
├── generation/            runtime (dynamic) resource generation, opt-in per machine
├── integration/           IntegrationTests (example machines), KubeJSDataGenFix
├── mixin/                 MachineBuilder, GTMachineUtils, OverclockingLogic, GTRecipeViewerWidget,
│   │                      fusion, multi smelter, tier skipping
│   ├── recipedb/          RecipeDB grouped search (own config gtmqol.recipedb.mixins.json)
│   ├── gtceufix/          workarounds for upstream bugs; delete once fixed upstream. BaseSchemaRendererMixin: MUI
│   │                      computes the multiblock preview's GL viewport from MUI's own transform only, so in
│   │                      EMI/JEI (pose-translated) it is drawn at the screen's top-left; now uses the graphics pose
│   └── eap/               ExtendedAE Plus smart doubling (own config gtmqol.eap.mixins.json)
├── modular/               modular multiblock versions of single-block machines
├── multiblock/            Smart Assembly Factory, DTFR
├── overclock/             replacement OC logics
├── recipedb/              non-mixin side of the grouped search, RecipeDBMixinPlugin
├── steam/                 advanced steam multiblocks, steam parallel hatch, steam magical assembler
└── wireless/
    ├── WirelessBindingTrait.java, WirelessNetworks.java, NetworkId.java, FTBTeamsCompat.java, IOStats.java
    ├── WirelessCovers.java  cover definitions and items
    ├── WirelessRecipes.java magical assembler recipes for every wireless part
    ├── steam/             everything steam specific
    └── energy/            everything EU specific
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

Datagen gotcha, recurring: `MultipleArgumentsForOptionException: Found multiple arguments for option
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
because values are needed during CONSTRUCT. Options: `modularMachines.enabled` (default true),
`overclocking.*` (see Overclocking) and
`integrationTests.enabled` (default false); the latter registers `gtmqol:runtime_single_block` and
`gtmqol:runtime_multiblock`, never during datagen.

Mixin-time options can't use it (mixin configs load before mods). They go in
`config/gtmqol-early.properties`, read by `recipedb/RecipeDBMixinPlugin` with `java.util.Properties`
(same approach as GTCEu's `gtceu-early.properties`): `recipeDB.groupedSearch` (default true).

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

- `GTMachineUtilsMixin` injects at RETURN of `GTMachineUtils.registerTieredMachines`, which every tiered
  single-block machine goes through (gtceu's, our magical assembler, other addons'). `ModularMachines.register`
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
- Config `modularMachines.enabled` (default true). Not ported: KubeJS tiered machine hook, the
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
  assembler (4 HP steam machines or 1 LV electric machine + 4 bronze gearbox + 8 bronze bricks + bronze
  plates/gears + 4 ULV circuits, ULV EU/t, so the steam one can make them); parallel hatch from a steam
  hatch + 2 bronze gearboxes + 4 LV circuits.
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
  click; the column is now skipped when the handler is the steam tank (the steam one has no recipe fluid input).
- Ghost circuit on steam: `SimpleSteamMachineMixin` attaches `ProgrammableCircuitSlotTrait` (as "circuit", like
  `SimpleTieredMachine`) to every steam single block, GTCEu's included; `GTSingleblockMachinePanelsMixin` turns
  trait configurators back on for the steam panel (`defaultSteamMachinePanelBuilder` disables them) so the slot
  shows; `SteamItemBusPartMachineMixin` re-enables the bus circuit slot GTCEu turns off (input bus only), which
  gives steam multis ghost circuits through their input bus.

## RecipeDB grouped search (`recipedb/`, `mixin/recipedb/`)

Port of the old `RecipeDBMixin` (late-game lag fix). Full write-up, including what the 7.x patches did and
why most of them are obsolete in v8, in `docs/RECIPEDB_REFACTOR.md`. In short:

- `fromHolder` is overwritten to tag each ingredient entry with its `RecipeHandlerList` group.
- Pattern buffers are split per worker, with the circuit and shared inventories as catalysts.
- `RecipeIterator`'s branch pushes are filtered to entries one group could supply together.
- The whole set is gated by `RecipeDBMixinPlugin` (`recipeDB.groupedSearch` in
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
  constructor, recipe types / machines from the GTCEu register events. Not ported: the sticky card and the
  oblivion singularity (electric implosion is gone). Machine names come from `SimpleMachineBuilder`'s
  `toEnglishName`, so they read "Me Assembler".
- Smart doubling (ExtendedAE Plus, optional): `gtmqol.eap.mixins.json`, gated by `ae2/EAPMixinPlugin`
  (`LoadingModList` has `extendedae_plus`). `mixin/eap/MEPatternBufferSmartDoublingMixin` on GTCEu's buffer
  (so ours too) implements `ISmartDoublingHolder`, `@SaveField` toggle (default on) and limit (0 = none),
  copies them onto the slot patterns (`ISmartDoublingAwarePattern`, `PatternScaler.getComputedMul`) at
  `getAvailablePatterns` HEAD and in the setters. `pushPattern` unwraps `ScaledProcessingPattern` to its
  original for the slot/worker matching (and `worker.pattern`, so refunds still match) but pushes the scaled
  one's inputs (`@ModifyArg` on both `InternalSlot.pushPattern`). The user said this unwrap is required.
  UI: a "×2" left configurator opening a popup (toggle + limit), added by `@ModifyReturnValue` on
  `getPanelBuilder` (`SmartDoubling.addConfigurator`). Lang keys registered in `AE2Machines`.
- Versions are constrained by GTCEu's JEI mixins: JEI stays 15.20.0.115, so EAP stays 1.6.1 (see
  `gradle.properties`).
- Known, ignored for now: a JVM access violation (C2 JIT, `InventoryChangeTrigger`) once while picking up a
  buffer.

## Pending / open

- Open question: should the accessor also use the rainbow overlay?
- AE2 pattern encoding preferring universal circuits (old `EncodingHelper` mixin) — needs a mixin, waiting
  for the user's go-ahead.
- Done: jar naming is `gtmqol-<mod version>-<mc version>.jar` (currently `gtmqol-2.0.0-1.20.1.jar`).
  `archives_base_name=gtmqol`; `build.gradle` sets `archiveVersion` on every `AbstractArchiveTask`
  (including Loom's `remapJar`). `project.version` stays `mod_version` (2.0.0), which is what goes into
  `mods.toml`. More naming details to be added later.

## User preferences not covered by `CLAUDE.md`

- `README.md` is entirely in English. Credits for borrowed ideas and assets go in its Credits section,
  never in code comments.
- The user replies in Chinese.
- The user runs all Gradle tasks (build, `runData`, `runClient`) and pastes logs back; tell them exactly
  what to look for in game.
