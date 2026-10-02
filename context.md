# GTMQoL Current Context

Handoff notes for continuing work in a new session or on another machine. Build environment, dependency
pitfalls and working rules live in `CLAUDE.md`; read that first. This file covers what exists, why it is
shaped this way, and what is pending.

## State

- Branch `migrate/gtceu-v8-runtime-generation`. All work is committed.
- Builds, datagen and the dev client all work. Everything below has been tested in game by the user.
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
    ├── WirelessBindingTrait.java, WirelessNetworks.java, NetworkId.java, FTBTeamsCompat.java   (generic)
    └── steam/             everything steam specific
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

## Pending / open

- **Abstraction for a future EU network: proposed, not confirmed, not implemented.** The idea:
  - A generic wireless storage keyed by resource type (steam, EU), each with `Map<NetworkId, BigInteger>`
    plus `IOStats`.
  - `FTBTeamsCompat`, "To team" and stats sampling iterate over all resource types. This also removes the
    current wrong-direction dependency of `wireless/` on `wireless/steam`
    (`WirelessBindingTrait.movePrivateToTeam`, `WirelessNetworks.onServerTick`, `FTBTeamsCompat`).
  - The shared UI parameterized by the stored-amount getter and unit text.
  - No base machine class.

  Per `CLAUDE.md`, ask the user before doing any of this.
- Open question: should the accessor also use the rainbow overlay?

## User preferences not covered by `CLAUDE.md`

- `README.md` is entirely in English. Credits for borrowed ideas and assets go in its Credits section,
  never in code comments.
- The user replies in Chinese.
- The user runs all Gradle tasks (build, `runData`, `runClient`) and pastes logs back; tell them exactly
  what to look for in game.
