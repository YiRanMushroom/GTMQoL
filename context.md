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

## 1.21.1 port (branch `port/1.21.1`)

- Branched from `87a49de` (GTNL steam machines). The plan is two long-lived branches kept in sync with
  cherry-picks. A multi-version tool such as Stonecutter is only an idea for now and has not been agreed.
- Build setup:
  - ModDevGradle 2.0.141 on NeoForge 21.1.248 with Java 21.
  - Dev-only mods go in a `localRuntime` configuration.
  - Mixin configs are listed in `neoforge.mods.toml`.
  - Mixin `compatibilityLevel` is `JAVA_21` and `pack_format` is 34.
  - Versions follow gtceu's 1.21 branch (`gradle/libs.versions.toml`, `forge.versions.toml`).
- `compileJava` passes. Not run in game yet.
- Code differences from master (keep these in mind when cherry-picking):
  - gtceu 1.21 registers everything as deferred Registrate entries. Machine, cover and recipe type fields
    are `MachineEntry` / `Holder<CoverDefinition>` / `GTRecipeTypeEntry`, not definitions. Content is
    declared in the `GTMQoL` constructor (recipe types first). `IGTAddon` has no `initializeAddon`.
  - Builders are in `api.registry.registrate.builder`. `register()` returns `MachineEntry`, and the
    properties live behind `properties()`. `RuntimeGeneration` keeps the builder and the entry.
  - `ModularMachines`: `GTMachineUtilsMixin` only queues the tiered entries and the builder's recipe type
    suppliers (`@Share` between the `BiFunction.apply` wrap and the RETURN inject; read through
    `MachineBuilderAccessor`). The multiblocks are declared at the start of the `gtceu:machine`
    `RegisterEvent`, at NORMAL priority. That is before our registrate's LOW `onRegister` and after
    recipe types exist. The config is readable by then.
  - `FEInputProvider` registers `Capabilities.EnergyStorage.BLOCK` in `RegisterCapabilitiesEvent` for
    every `MetaMachineBlock` / `CableBlock`. gtceu registers first, so its own FE storages win.
  - Data stick binding uses `DataComponents.CUSTOM_DATA` / `CUSTOM_NAME`. SavedData uses
    `SavedData.Factory` plus `HolderLookup.Provider`.
  - `CircuitTags` uses `c:` (Mekanism 1.21: `c:circuits/<tier>`, `c:alloys/advanced`).
  - The dev recipe viewer is EMI, not JEI. That matches gtceu's 1.21 dev setup. With JEI, this gtceu snapshot
    crashes in `GTRecipeCategories.<clinit>`: `CategoryIcon` calls `GTJEIPlugin.getRuntime()` during mod
    construction, and it is still null there.
  - EAP is pinned by Modrinth version id, because the Forge and NeoForge builds share version numbers. The
    NeoForge EAP's `ISmartDoublingHolder` only has the limit, so the smart doubling on/off is our own
    mixin method, passed to `SmartDoubling.addConfigurator`.
  - The AE2 dev runtime is 19.2.18, because EAP 1.6.3 needs `AEBaseMenu.clicked`. gtceu builds against 19.2.8.
  - The mixin configs' `mixinextras.minVersion` is 0.5.3, the version NeoForge 21.1.248 bundles. It is 0.5.5 on master.
  - `GTMQoLAddon` calls `defaultCreativeTab((ResourceKey) null)`, same as gtceu's `GTRegistration`. Registrate's
    default tab is SEARCH, so otherwise every item adds itself to it a second time and NeoForge throws
    `already exists in the tab's list`.
  - Workarounds for gtceu 1.21 bugs live in `core/mixins/gtceufix/` (1.21 only). Delete each one once upstream fixes it:
  - `gtceufix/EmiCallWrapperMixin`: when you click the recipe type button in a machine UI, gtceu calls the
    private `EmiApi.setPages` through `EmiApiAccessor`. That invoker is never applied to `EmiApi`, so the click
    throws `NoSuchMethodError`. The mixin switches it to the public `EmiApi.displayRecipeCategory(machineCategory)`,
    which no longer shows the other categories of the same recipe type as tabs. Because of this, EMI is now `compileOnly`.
  - `gtceufix/GameDataMixin`: `GTRegistries` adds `gtceu:recipe_type` to the load order, and gtceu's own
    `GameDataMixin` maps it to the vanilla recipe type registry. That means vanilla `recipe_type` gets posted a
    second time (under a gtceu key, only to make KubeJS registration easier). Mods that handle the event by
    `event.getRegistry()` (Core Lib, CyclopsCore) then throw duplicate-registration errors, and the ATM10 pack
    fails to load. The mixin WrapOperations `ModLoader.postEventWrapContainerInModOrder` so that this duplicate
    event only goes to gtceu and kubejs. ModernFix @Redirects the same call; that is why it's a WrapOperation.
    gtceu also moves its own registries and `recipe_type` ahead of `attribute`. CyclopsCore assumes ATTRIBUTE is
    the first event and queues every entry at that point, so it throws `Tried registering ... after its
    registration event`, and the cascade leaves `neoforge:swim_speed` unbound. `getRegistrationOrder` is now
    `@ModifyReturnValue`d to put ATTRIBUTE back at the front.
  - `gtceufix/GTMuiWidgetsMixin` works around a gtceu 1.21 bug: in
    `GTMuiWidgets.createCircuitSlotSyncValue`, the setter calls `IntCircuitBehaviour.stack(v, current.getCount())`.
    When the slot is empty the count is 0, which gives an empty stack, so the ghost circuit can never be set.
    The mixin clamps the count to ≥1. 1.20 uses `stack(v)`, so master doesn't need it. Remove the mixin once
    upstream fixes this.
  - `gtceufix/BaseSchemaRendererMixin` (in `client`, **on both branches**) works around a ModularUI bug: when
    `BaseSchemaRenderer.draw` computes the GL viewport it only uses MUI's own `transformX/Y`. The recipe UI that
    EMI/JEI embeds is translated through `PoseStack`, so the multiblock preview gets drawn at the screen's top-left.
    The mixin WrapOperations `Viewport.calculateOpenGLViewportFromRectangle` and computes the origin from
    `context.getLastGraphicsPose()` instead. In a normal MUI screen the pose is exactly MUI's matrix, so nothing
    changes there.
  - `gtceufix/GTRecipeCategoryMixin`: in 1.21, `GTRecipeCategory.getLanguageKey()` is
    `recipe_category.<ns>.<path>`, but Registrate only generates `recipe_type.<ns>.<path>` for recipe types. gtceu's
    own lang has no `recipe_category.*` either, so every category name in EMI/JEI shows as the raw key. For a default
    category (id == recipe type id), the mixin returns the recipe type's key instead. Extra categories (KubeJS,
    `GTRegistrate.recipeCategory`) are unchanged. 1.21 only (the user reported this on 1.21).
  - `gtceufix/MEPatternBufferInternalSlotMixin`: a pattern buffer `InternalSlot` stores contents keyed by count-1
    stacks (`AEItemKey.toStack()`, `AEFluidKey.toStack(1)`), with the real amount kept in a long. In 1.21,
    `handleItemInternal`/`handleFluidInternal` match with NeoForge's `SizedIngredient.test` /
    `SizedFluidIngredient.test`, which also require `count >= ingredient count`. So any ingredient needing more than 1
    fails: with parallel, even a one-in/one-out recipe from a single pattern shows "insufficient item". The
    parallel count itself (`getMaxByInput`) is computed correctly. The mixin only tests the item/fluid. The 1.20.1
    gtceu `SizedIngredient.test` ignores the count, so this is 1.21 only.
  - `gtmqol.jeifix.mixins.json` (`gtceufix/jei/JeiRecipeSlotMixin`, gated by `core/JeiRecipeSlotFixPlugin`;
    1.21 only, since master's JEI is pinned to 15.20): JEI 19.46+ dropped the `RecipeSlot.allIngredients/
    displayIngredients` fields (replaced by `RecipeSlotIngredients`), and ModularUI's `jei.RecipeSlotAccessor` (in a
    required config) still targets them. So `RecipeSlot` fails to load and every recipe that goes through JEMI in EMI
    breaks (ATM10's JEI 19.57; Create shows nothing). The mixin adds the two fields back (priority 500, applied before
    MUI's accessor). JEI doesn't read them, and MUI only writes them for its own JEI categories. The plugin applies the
    mixin only when JEI is present and the fields are missing (it reads the class through MixinService's bytecode provider).
  - `KubeJSDataGenFix` is removed, since NeoForge's `DatagenModLoader` sets the flag correctly.
    Check that datagen exits.
- Mixin targets were checked statically against the 1.21 sources and all match. Runtime still unverified.
- `src/generated/resources` still has the 1.20 layout (`data/forge/tags/items`). Regenerate it with `runData`.

## Source layout

Loosely follows GTCEu Modern (`api` / `common` / `data` / `core` / `integration` / `client` / `config`), with
features kept together as subpackages of `common/` rather than split by kind (machines/items/recipes) the way
GTCEu does. 1.21.1 only so far (restructured 2026-10-03); 1.20.1 still has the old flat layout, so apply the
same move there before cherry-picking anything.

```text
src/main/java/com/yiran/minecraft/gtmqol/
├── GTMQoL.java            entry point, GTMQoL.id(), what gets registered (config gated)
├── GTMQoLAddon.java       @GTAddon, owns the GTRegistrate, creative tab, machine()/multiblock() helpers,
│                          addRecipes (config gated)
├── api/generation/        runtime (dynamic) resource generation, opt-in per machine
├── client/                client-only init and renders (DTFR ring)
├── config/                GTMQoLConfig (toma, runtime), EarlyConfig (properties, mixin time)
├── common/
│   ├── assembler/         MagicalAssembler (recipe type, tiered machines, its own recipes) and its UI
│   ├── circuit/           UniversalCircuits, ControlCircuits (items)
│   ├── fe/                FEInputProvider: FE input for every GT machine and cable
│   ├── modular/           modular multiblock versions of single-block machines
│   ├── multiblock/        Smart Assembly Factory, DTFR, void miner, fishing pond
│   ├── overclock/         replacement OC logics
│   ├── recipedb/          non-mixin side of the grouped search
│   ├── steam/             advanced steam multiblocks, steam parallel hatch, steam magical assembler
│   ├── test/              IntegrationTests (example machines)
│   └── wireless/          WirelessBindingTrait, WirelessNetworks, NetworkId, FTBTeamsCompat, IOStats,
│       │                  WirelessCovers (cover definitions and items)
│       ├── steam/         everything steam specific
│       └── energy/        everything EU specific
├── data/
│   ├── recipe/            MiscRecipes, EarlyGameRecipes, WirelessRecipes
│   └── tag/               CircuitTags (GT ↔ Mekanism tags, datagen)
├── core/                  mixin config plugins: GTMQoLMixinPlugin, RecipeDBMixinPlugin, EAPMixinPlugin,
│   │                      JeiRecipeSlotFixPlugin
│   └── mixins/            gtmqol.mixins.json: MachineBuilder, GTMachineUtils, OverclockingLogic, fusion, steam, ...
│       ├── gtceufix/      workarounds for GTCEu bugs (jei/ has its own config gtmqol.jeifix.mixins.json)
│       ├── recipedb/      RecipeDB grouped search (own config gtmqol.recipedb.mixins.json)
│       └── eap/           ExtendedAE Plus smart doubling (own config gtmqol.eap.mixins.json)
└── integration/ae2/       pattern buffer abstraction, overclocked buffer, ME machines, smart doubling (AE2 only)
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
because values are needed during CONSTRUCT. Always read through `GTMQoLConfig.get()`, which registers it on
first use (like GTCEu's `ConfigHolder`): the fusion, output limit and boiler mixins read it from GTCEu's static
initializers, which run while GTCEu is constructed, before our constructor. toma's `registerConfig` only needs
the `@Config` id, not a mod loading context. During datagen `get()` returns a fresh `new GTMQoLConfig()` (all
defaults, nothing read or written), so datagen always generates lang/models for every feature, and turning a
feature off only skips registration at runtime.

Every feature has a toggle, all default true and restart-only, except `integrationTests.enabled` (default
false; registers `gtmqol:runtime_single_block` and `gtmqol:runtime_multiblock`, never during datagen):

- `machines.*`: `smartAssemblyFactory`, `dimensionallyTranscendentFusionReactor`, `voidMiner`,
  `advancedSteamMachines` (large steam multis, steam parallel hatch, steam magical assembler, the extra steam
  single blocks, as one unit), `electricImplosionCompressor`. `GTMQoLMultiblocks` has one init/recipe method
  per machine.
- `modularMachines.enabled`.
- `wireless.energy` / `wireless.steam`: machines, covers (`WirelessCovers.initEnergy/initSteam`) and recipes
  (`WirelessRecipes.addEnergyRecipes/addSteamRecipes`). `WirelessNetworks.init` (events, command, stats) always
  runs, the void miner uses the EU network too.
- `circuits.*`: `universalCircuits` (items + the magical assembler conversion), `controlCircuits`,
  `mekanismCircuitTags`.
- `recipes.*`: `miscRecipes`, `earlyGame` (`EarlyGameRecipes` and the kept sand → glass smelting),
  `keepManualCompression` (see the steam section), `keepVanillaTNT`, `netherStarDust` (see Nether stars).
- `steamTweaks.*`: `circuitSlots` (`SimpleSteamMachineMixin` circuit trait, `SteamItemBusPartMachineMixin`,
  `GTSingleblockMachinePanelsMixin`), `fluidTanks`, `noOutputLimits` (`GTMachinesMixin`/`GTMultiMachinesMixin`
  call the original), `optionalLargeBoilerParts`.
- `overclocking.*`: see Overclocking.
- `ae2.*`: `overclockedPatternBuffer`, `processing` (`AEProcessing`), `dualHatches` (`AEDualParts`),
  `patternBufferReturn` (runtime-read, no restart).
- `misc.feInput`.
- `voidMiner.dimensionMapping` (not a toggle).

Always on: the magical assembler (most recipes use it) and its own recipes (creative data hatch, rubber
sapling), and the GTCEu bug fixes. Mixins that are not `@Overwrite`s always apply and check the config at run
time.

Mixin-time options can't use it (mixin configs load before mods). They go in
`config/gtmqol-early.properties`, read by `config/EarlyConfig` with `java.util.Properties` (same approach as
GTCEu's `gtceu-early.properties`; missing keys are added with their defaults):
- `recipeDB.groupedSearch` (default true): `core/RecipeDBMixinPlugin` applies the whole recipedb config or not.
- `overclocking.overhaul` (default true): `core/GTMQoLMixinPlugin` (on `gtmqol.mixins.json`) skips
  `OverclockingLogicMixin`, `GTRecipeViewerWidgetMixin` and `GTRecipeModifiersMixin` when it is off.

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
  is linear in the amperage (powers of two are the worst: after dividing out the 2s it still counts up to
  amps/2). At our 2^24 A that is millions of iterations per call, and `EURecipeCapability.getMaxParallelAmount`
  calls it on every parallel calculation (seen as ~77% of a server profile, 2026-10-05). The one call in
  `EnergyContainerList`'s constructor (on form) is left as is.
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
- Toggle `misc.feInput` (the old implementation had `enableFEToEUConversion`).

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
  at `TagsUpdatedEvent`. The tags are datagen'd static JSON, so the `circuit` toggles (`universalCircuits`,
  `controlCircuits`, `mekanismCircuitTags`) don't remove them at runtime; known limitation, deliberately left
  as is.
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
- `GTRecipeModifiersMixin` (only needed with the overhaul): with the new OC computing parallels, the multi smelter's
  base → OC → parallel order gives wrong results, so the middle OC of `multiSmelterParallel` becomes identity
  and the OC is appended to the returned function, computed on the parallelized recipe.
- The three mixins above are switched together by `overclocking.overhaul` in `gtmqol-early.properties`
  (overwrites can't be switched off at runtime, so `GTMQoLMixinPlugin` decides whether they apply at all). The
  following have toggles under `overclocking.*` in `gtmqol.yaml` (all default true, restart):
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

Goal: nether stars before IV (IV needs a lot of them).
- `recipes.keepVanillaTNT`: `RecipeRemovalMixin` `@ModifyExpressionValue`s the `removeVanillaTNTRecipe` read in
  `RecipeRemoval.generalRemovals` to false, i.e. keeps `minecraft:tnt` crafting regardless of GTCEu's config.
- `recipes.netherStarDust`: mixer, 4 diamond dust + 16 silver dust → 1 nether star dust, 20 s, `VA[HV]`
  (`MiscRecipes.addNetherStarDust`, user's choice; GTCEu has no nether star dust synthesis, GTExpert's recipe
  needs rocket fuel at LuV). The dust then goes through GTCEu's normal implosion recipes.
- `machines.electricImplosionCompressor`: port of the 1.19 one. Recipe type and multiblock
  `gtmqol:electric_implosion_compressor` (old 1.19 pattern, robust tungstensteel casing, parallel hatch,
  perfect subtick OC, batch). Recipes: GTCEu makes four implosion variants per material (powderbarrel, TNT,
  dynamite, ITNT); only the one with vanilla TNT (`implode_<x>_tnt`) is copied, as `implode_<x>_electric` without
  the TNT and 4× duration. No mixins (1.19 used two): at common setup an `onSave` is chained onto
  `IMPLOSION_RECIPES`' prototype builder, which `recipeBuilder(id)` copies (recipes are built on server reload,
  after that). It keeps and calls any earlier `onSave` (implosion has no `onRecipeBuild` in GTCEu, so normally
  none); someone setting it after us without chaining would drop ours. GTCEu's `RecipeManagerLateMixin` also
  rebuilds every loaded `GTRecipe` with its type's prototype `onSave` at the end of `RecipeManager.apply`, so
  data pack / KubeJS implosion recipes get copies too (same ids overwrite, no duplicates). Controller: shaped `PCP/FSF/PCP`, ZPM circuits,
  implosion compressor, IV motors and field generators (as in 1.19).
- Not yet in game. Ported to 1.20.1 (there KubeJS implosion recipes get no copy, no late regeneration).

## Cyclic multiblocks (`multiblock/CyclicMultiblockMachine`, `PendingOutputTrait`)

Abstraction the user asked for (void miner + fishing pond). Written, not built yet.

- `CyclicMultiblockMachine extends MultiblockControllerMachine implements IMuiMachine`: own server tick, no
  recipe logic (GTCEu's voids overflowing outputs). State IDLE → WORKING (`cycleTicks()`, `onCycleTick()` every
  tick, pauses while unformed) → `finishCycle()` adds to `output` → OUTPUTTING (retry every `RETRY_TICKS` = 5)
  → IDLE. While enabled in IDLE, `startCycle()` (returns null or a problem lang key, also pays) is retried every
  5 ticks. Stopping lets the current cycle finish. `RECIPE_LOGIC_STATUS` set by hand (WORKING only while working).
  Shared UI pieces: status line, stored button + live popup (fixed rows, `setEnabledIf`), power toggle, popup,
  counter row (Shift step per machine). Lang keys under the machine's prefix, common ones via
  `GTMQoLMultiblocks.addCyclicMachineLang`.
- `PendingOutputTrait`: parallel `@SaveField` lists `items` (count-1 templates, merged by
  `isSameItemSameComponents`, so enchanted books / damaged rods survive) and `counts` (long). `output()` inserts
  into every `NotifiableItemStackHandler` with `IO.OUT` (ME output bus works). Breaking the controller loses it.
- The refactor changed the void miner's saved fields (`pending` CompoundTag gone, state `MINING` → `WORKING`):
  old placed miners lose their pending ores / may load in an odd state.

## Void miner (`multiblock/VoidMinerMachine`, `VoidMinerOres`)

Ported to 1.20.1 (2026-10-07, not built there yet). On `CyclicMultiblockMachine` (see above).

- No energy hatch (`DUMMY_RECIPES`). Shape = GTCEu's EV Large Miner with solid steel casing and steel frames;
  'X' only takes output buses (≥1). Model copies the large miner's (active parent when formed).
- Power: `WirelessBindingTrait` (auto-binds on placement, data stick works) and
  `WirelessEnergySavedData.extract(network, 1, cost)` = all-or-nothing. 1M EU per stack (64 ores),
  operations 1..16 × stacks 1..16, paid in `startCycle()`.
- Semantics (user-corrected): one stack = 64 of the *same* ore. Each operation draws one ore (binary search
  over the cumulative chances) and yields `multiplier` stacks of it, so operations = max distinct ores per cycle.
  Settings are snapshotted into `cycleOperations`/`cycleMultiplier` at payment; changes apply next cycle.
- Cycle 300 ticks; the next cycle only starts once everything is out, so at most 16 ore types are pending.
- Ores: GTCEu `ORE_VEIN` datapack registry ("GTNH veins" read as GT's own veins), veins whose
  `dimensionFilter` has the dimension; each vein adds `weight × chance / Σchances` per material. Prefix:
  overworld deepslate, nether netherrack, end endstone, else the `TagPrefix.ORES` entry whose stone is the
  dimension's noise `defaultBlock`, else deepslate. `voidMiner.dimensionMapping` (`"from=to"`) mines another
  dimension's veins and stone. Computed once per machine load (a datapack `/reload` needs a chunk reload).
- UI: wireless binding block (`WirelessEnergyUI.create`) + status + settings lines + buttons: ore chances popup,
  stored popup, settings popup (±1, Shift ±4), power toggle. Popups are `syncedPanel`s.
- Recipe (magical assembler, LV, no chips): 4 LV miners, 16 LV circuits, 16 each LV motor/piston/conveyor,
  16 solid steel casings, 64 double steel plates, 16 steel gears.

## Industrial Fishing Pond (`multiblock/FishingPondMachine`, `client/FishingPondWaterRender`)

Forms in game on 1.21.1 (2026-10-07); ported to 1.20.1 (not built there yet). Config `machines.fishingPond`.
On `CyclicMultiblockMachine`.

- Structure: GTCEu's Large Chemical Bath widened — 7 × 7 × 5 watertight casing, 5 × 5 × 4 air cavity
  (= vanilla's open-water check: 5 × 5, y−1..y+2), open top, controller in the front wall at the cavity's bottom
  layer. 'X' ≥100 casings, ≥1 output bus, ≥1 `INPUT_ENERGY` hatch (only that ability, user's call). Chem bath model.
- Pattern needs an explicit `startOffset(OriginOffset.of(BACK, 6).move(DOWN, 1).move(LEFT, 3))`: GTCEu's
  automatic offset counts a repeatable slice once, so with `sliceRepeatable(5, 5, ...)` before the controller's
  slice it checks the wrong blocks and never forms (GTCEu bug). `startOffset` = vector from the controller to the
  first char of the first slice.
- Water: persistent `MultiblockFluidRendererTrait` (offsets = the 5 × 5 on the cavity's second layer from the
  top) + own `DynamicRender` (always water, same `FluidBlockRenderer` settings as GTCEu's chem bath);
  `FluidAreaRender` can't be reused (typed to `WorkableMultiblockMachine`, reads recipe logic).
- Rod: `CustomItemStackHandler(1)` in the controller UI, filter `canPerformAction(FISHING_ROD_CAST)`, dropped
  via `modifyDrops`, never damaged. No rod = start problem.
- Energy: while working, drains energy inputs as fast as they give into `energyBuffer` (saved), capped at
  Σ(V × A) × 100 (saturating). Never power-fails. Cycle 100 ticks. At the end: cost per fish
  c = 1000 EU (×4 treasure) × (350 − min(Lure ticks, 300)) / 350, rounded up (Lure I/II/III 715/429/143);
  m = buffer / (rolls × c); m < 1 → nothing, buffer kept; else pay m × rolls × c, roll vanilla `FISHING` loot
  `rolls` (1..64, UI) times, every stack × m.
- Loot: no AT/mixin. Normal mode leaves out THIS_ENTITY, so the treasure entry's `in_open_water` condition fails
  (fish + junk). Treasure mode passes a fresh `FishingHook` (not added to the level; `openWater` defaults true).
  Luck of the Sea → `withLuck(getFishingLuckBonus)`. Origin = controller.
- Recipe (magical assembler, EV): 4 EV fishers, 8 EV circuits, 8 EV pumps, 4 EV robot arms, 32 watertight
  casings, 32 double titanium plates, 16 fishing rods.

## Crystal Growth Chamber (`crystal/CrystalGrowth`, `crystal/CenterBlockCondition`)

Works in game on 1.21. Port to 1.20 still to do. Config `machines.crystalGrowthChamber`.
- 3 × 3 × 3 steel casing, controller in the middle of the front face, any block in the middle. The middle of the
  four sides and the back: tempered glass or casing. `NON_Y_AXIS`, no extended facing: the condition reads
  `pos.relative(front.getOpposite())`.
- Perfect subtick OC only: no parallel hatch (`autoAbilities(true, false, false)`), no batch mode (user's call).
- Plain GT recipes (`gtmqol:crystal_growth`), one per budding block: `CenterBlockCondition(block)` + the shard as a
  `notConsumable` input → 4 shards, LV, 10 s. Its `modifyUI` shows the block like GTCEu's `AdjacentBlockCondition`.
- Why the shard input: `RecipeDB.addRecursive` keeps one recipe per set of item/fluid inputs and drops the rest
  (dev-only warn, not in the category map either, so not in EMI). Recipes with no item/fluid input are added to the
  category map but never found by the lookup (`fromHolder` returns null). Failing conditions are fine though:
  `handleSearchingRecipes` goes on to the next match.
- Recipes generated at runtime (`addRecipes`): `ae2:flawless_budding_quartz` → `certus_quartz_crystal`, plus every
  `<ns>:budding_<x>` that has an `<ns>:<x>_shard`, in any namespace. That covers vanilla amethyst and all 28 GeOre
  crystals: GeOre's `GeOreBlockReg` registers exactly these names on both 1.20 and 1.21, and its cluster loot is the
  shard. Missing ids are skipped.
- KubeJS: GTCEu registers a schema for every GT recipe type, so `event.recipes.gtmqol.crystal_growth(id)` exists;
  a binding for the condition is still to do.

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
- Manual compression (3x3/2x2 block/ingot/nugget, raw ore block crafting): setting GTCEu's
  `ConfigHolder.INSTANCE.recipes.disableManualCompression = false` from our constructor did not work in game.
  Now `ManualCompressionMixin` `@ModifyExpressionValue`s every read of that field (`GETFIELD`) to false when
  `recipes.keepManualCompression` is on: `RecipeAddition.disableManualCompression`, `RecipeRemoval.init`,
  `MaterialRecipeHandler.processNugget`/`processBlock`, `OreRecipeHandler.processRawOre` (one mixin, four
  targets, each has at least one match). Recheck the list after a GTCEu bump (grep `disableManualCompression`).

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
  buses/hatches/pattern buffer (+proxy) from AE2 parts, and wiremill/polarizer/mixer AE recipes. Not ported:
  the oblivion singularity (electric implosion is gone; the sticky card is below). Machine names come from
  `SimpleMachineBuilder`'s `toEnglishName`, so they read "Me Assembler".
- Smart doubling (ExtendedAE Plus, optional): `gtmqol.eap.mixins.json`, gated by `core/EAPMixinPlugin`
  (`LoadingModList` has `extendedae_plus`). `mixin/eap/MEPatternBufferSmartDoublingMixin` on GTCEu's buffer
  (so ours too) implements `ISmartDoublingHolder`, `@SaveField` toggle (default on) and limit (0 = none),
  copies them onto the slot patterns (`ISmartDoublingAwarePattern`, `PatternScaler.getComputedMul`) at
  `getAvailablePatterns` HEAD and in the setters. `pushPattern` unwraps `ScaledProcessingPattern` to its
  original for the slot/worker matching (and `worker.pattern`, so refunds still match) but pushes the scaled
  one's inputs (`@ModifyArg` on both `InternalSlot.pushPattern`). The user said this unwrap is required.
  UI: a "×2" left configurator opening a popup (toggle + limit), added by `@ModifyReturnValue` on
  `getPanelBuilder` (`SmartDoubling.addConfigurator`). Lang keys registered in `AE2Machines`.
- ME dual parts (`AEDualParts`, LuV, written, not built or tested): `me_dual_input` (extends the stocking bus, adds a
  stocking fluid list; main UI has items on the left, fluids on the right. `FluidConfigWidget` is a trimmed copy
  of GTCEu's `AEConfigWidget` with prefixed sync names, because the original's fixed action names collide when two
  sit in one panel; resync it if GTCEu changes the widget. The settings popup has item and fluid columns, with
  separate min stack size / ticks per cycle for fluids) and `me_dual_output` (extends the output bus, adds a fluid buffer;
  autoIO inserts into the network every tick instead of every `updateIntervals`; `buildMainUI` is a copy of the
  output bus's, whose waiting list syncs a `KeyStorage` refilled from both buffers, since the item buffer is private).
  Recipes: ME assembler (assembler if `ae2.processing` is off), stocking bus + stocking hatch / output bus +
  output hatch + 1000 mB glue + 144 mB (1 L) soldering alloy. Data stick shares the "MEInputBus" key, fluids only paste
  between duals.
- Pattern buffer return (`PatternBufferReturn`, `MEPatternBufferReturnMixin`): an extra OUT `RecipeHandlerList`
  (HIGHEST priority, undyed) added to `getRecipeHandlers` once and cached (identity matters). It inserts every
  output of the multiblock into the network (AE2 hands it to waiting crafting CPUs); if the network refuses,
  the other output parts get it. Per-buffer toggle (left configurator "ME", default on).
- Sticky card and universal circuit encoding, ported from the v7 code in `reference/` (written, not built or
  tested). `gtmqol.ae2.mixins.json`, gated by `core/AE2MixinPlugin` (AE2 loaded; the ExtendedAE mixin also needs
  `extendedae`; note ExtendedAE's mod id is `extendedae`, v7's 1.20 fork used `expatternprovider`). Package
  `core/mixins/ae2`:
  `EncodingHelperMixin` (`@ModifyExpressionValue` on the `Comparator.comparing` in `<clinit>`, adds "is a universal
  circuit" after craftable, so patterns encode with the universal circuit); `MEInventoryHandlerMixin` (adds `ISticky`:
  flag + `shouldStick` = sticky, non-empty partition, passes it); `NetworkStorageMixin` (a `@Share` stop flag, set
  after an insert into a sticky handler that has the key partitioned; wrapped iterators of `priorityInventory.values()`
  and `secondPassInventories` stop on it); `StorageBusPartMixin` / `PartSpecialStorageBusMixin` (ExtendedAE's mod /
  precise / tag buses) set the flag from `isUpgradedWith(STICKY_CARD)` right at the `setVoidOverflow` call in
  `updateTarget`. `integration/ae2/StickyCardItem` (item, tooltip lang, `Upgrades.add` in `FMLCommonSetupEvent`,
  recipes: ME assembler if `ae2.processing`, plus the shaped one). Two early switches in
  `config/gtmqol-early.properties` (`EarlyConfig`, not toma, since they decide mixins): `ae2.universalCircuitEncoding`
  (only `EncodingHelperMixin`) and `ae2.stickyCard` (the other four mixins, the item and its recipes); the plugin
  applies each set accordingly, and the mixins also check `StickyCardItem.STICKY_CARD != null`. `StorageBusPartMixin` `@Shadow`s the `handler` field; its private type `StorageBusInventory` is opened by `META-INF/accesstransformer.cfg`, which MDG only applies to Minecraft, so the ModAccessor plugin (`dev.vfyjxf.modaccessor`, build time only) patches the AE2 jar on `accessCompileOnly` for javac (compiles; a `@WrapOperation` with the supertype receiver did not match). The Upgrades have no tooltip group (AE2's storage bus
  upgrades have none). ExtendedAE is now `compileOnly` too.
- Versions are constrained by GTCEu's JEI mixins: JEI stays 15.20.0.115, so EAP stays 1.6.1 (see
  `gradle.properties`).
- Known, ignored for now: a JVM access violation (C2 JIT, `InventoryChangeTrigger`) once while picking up a
  buffer.

## Pending / open

- Known, not fixing (GTCEu issue, 2026-10-06): on steam (BRONZE theme) machine panels, including our wireless steam ones, 
  the title and the GT logo are pushed to the right and look squeezed. GTCEu's own steam machines do the same, 
  so it is upstream; revisit only if GTCEu changes the panel.
- Fixed (gtceu bug, 2026-10-03): the ME stocking / plain ME input bus config UI (and our dual input) didn't
  update while open, only after reopening. The server sent and the client received the packet, but the display
  stayed old. Likely cause: `AEConfigSyncHandler.detectAndSendChanges` computes the per-slot `changed` flag and
  updates its cache inside the `syncToClient` writer lambda, which runs at packet encode time; if encoding runs
  it twice, the delivered packet has every flag false (`init` forces true, so opening was always right). Fix:
  `gtceufix/AEConfigSyncHandlerMixin` (`@WrapMethod`, resend everything when anything changed); confirmed in
  game, the encode-twice part itself unverified. Remove it once gtceu fixes this.
- Fixed: the dual input's fluid config was lost on world reload because the fluid list was only `attachTrait`ed;
  trait data needs `attachPersistentTrait(name, trait)`. (Not yet re-tested in game.)

- Open question: should the accessor also use the rainbow overlay?
- GTCEu v8 terminal (`TerminalBehavior`, not a bug of ours): Shift+right-click auto-build only works in creative,
  and only after the preview has been opened and closed once (right-click controller → right-click air → close;
  closing stores the schema). Otherwise `useOn` passes and `use` opens the preview. No survival auto-build;
  state is global to all terminals (their FIXME).
- Ability audit of our multis (user asked to drop unused ones): SAF = recipe-type auto abilities (item/fluid in,
  item out, energy) + maintenance + parallel + data hatch; DTFR = fluid in/out + parallel + energy; steam multis
  = recipe-type buses/hatches + steam buses + steam hatch + steam parallel. All are used; only maintenance is
  questionable. Nothing removed yet, waiting for the user.
- Known bug, not fixed (user: leave it for now; workaround: don't reload client resources, restart if hit):
  after a client resource reload (F3+T, resource pack / language / mipmap change) every GTCEu bronze/steel themed
  UI (steam single blocks, steam generators) fails to open with `ClassCastException: IDrawable$2 (NONE) cannot
  be cast to UITexture` in GTCEu `MachineUIPanel.<init>`. Seen in a pack (ATM10 To the Sky, 2026-10-02); dev
  only loads themes once, so it looks fine. Cause is ModularUI (3.3.1-SNAPSHOT): `ThemeManager` merges the
  java-registered theme into a new `JsonBuilder` with `addAllOf` (shallow), then `parse` writes
  `background: "none"` (hover marker, when `panel` has no hover theme) into the shared `panel` object, i.e.
  into GTCEu's registered `GTGuiTheme` JSON. The next reload reads `"none"` as the panel background. Possible
  fix: client mixin in `gtceufix/` wrapping that `jsonBuilder.addAllOf(builder)` to pass
  `builder.getJson().deepCopy()`. 1.20.1's MUI not checked.
- Done: jar naming is `gtmqol-<mod version>-<mc version>.jar` (currently `gtmqol-2.0.0-1.21.1.jar`).
  `archives_base_name=gtmqol`; `build.gradle` sets `archiveVersion` on every `AbstractArchiveTask`.
  `project.version` stays `mod_version` (2.0.0), which is what goes into `neoforge.mods.toml`. More naming
  details to be added later.

## User preferences not covered by `CLAUDE.md`

- `README.md` is entirely in English. Credits for borrowed ideas and assets go in its Credits section,
  never in code comments.
- The user replies in Chinese.
- The user runs all Gradle tasks (build, `runData`, `runClient`) and pastes logs back; tell them exactly
  what to look for in game.
