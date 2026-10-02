# GTMQoL Current Context

Handoff notes for continuing work in a new session or on another machine. Build environment, dependency
pitfalls and working rules live in `CLAUDE.md`; read that first. This file covers what exists, why it is
shaped this way, and what is pending.

## State

- Branch `migrate/gtceu-v8-runtime-generation`. The wireless EU network is uncommitted.
- Builds, datagen and the dev client all work. Everything below except the EU network has been tested in
  game by the user.
- Minecraft 1.20.1, Forge 47.4.1, GTCEu v8 snapshot (pinned, see `gradle.properties`), Java 17 target,
  Architectury Loom 1.13. Java only; no Kotlin.
- The old implementation is archived under `reference/` as research material. Do not migrate it wholesale
  and do not modify it.

## Source layout

```text
src/main/java/com/yiran/minecraft/gtmqol/
├── GTMQoL.java            entry point, GTMQoL.id(), event wiring
├── GTMQoLAddon.java       @GTAddon, owns the GTRegistrate, creative tab, machine()/multiblock() helpers
├── config/GTMQoLConfig.java
├── generation/            runtime (dynamic) resource generation, opt-in per machine
├── integration/           IntegrationTests (example machines), KubeJSDataGenFix
├── mixin/MachineBuilderMixin.java
└── wireless/
    ├── WirelessBindingTrait.java, WirelessNetworks.java, NetworkId.java, FTBTeamsCompat.java, IOStats.java
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
  Only the integration-test machines use it now. Background in `docs/DYNAMIC_GENERATION.md` and
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
because values are needed during CONSTRUCT. Only option: `integrationTests.enabled` (default false); it
registers `gtmqol:runtime_single_block` and `gtmqol:runtime_multiblock`, never during datagen.

## Wireless steam network (the current feature)

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

## Wireless EU network (`wireless/energy/`) — written, NOT yet compiled or tested

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

## Pending / open

- Open question: should the accessor also use the rainbow overlay?

## User preferences not covered by `CLAUDE.md`

- `README.md` is entirely in English. Credits for borrowed ideas and assets go in its Credits section,
  never in code comments.
- The user replies in Chinese.
- The user runs all Gradle tasks (build, `runData`, `runClient`) and pastes logs back; tell them exactly
  what to look for in game.
