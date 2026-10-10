# Wireless networks, covers, EU ↔ FE

Split out of `context.md`. On both branches.

## Networks and binding (generic, `wireless/`)

A global pool per network, inspired by GTMThings' wireless energy.

- `NetworkId(id, private)`:
  - A team network uses the FTB team id. Without FTB Teams it is the player's UUID, i.e. their personal team.
  - A private network uses the player's UUID. Same UUID, different pools.
- Machines store only the bound player and a private flag (`WirelessBindingTrait`, `@SaveField @SyncToClient`).
  The team is looked up on every access (`WirelessNetworks.networkId`), so joining or leaving a team switches
  networks immediately.
- `FTBTeamsCompat` is optional (compile-only + dev runtime, not in `mods.toml`).
- Binding rules:
  - Placed by a real player → bound to them, in team mode (`BlockEvent.EntityPlaceEvent`, LOWEST priority,
    `FakePlayer` skipped). Anything else (Building Gadgets, fake players) stays unbound.
  - Unbound machines: anyone can bind them via the UI button.
  - Unbind, toggle private and "To team": only the bound player or ops (permission level 2).
  - "To team" moves the whole private pool into the team pool and switches the machine to team mode.
- Data stick (the machine implements `IDataStickInteractable` and delegates to the trait), GTMThings/GTCEu style:
  - Shift-right-click copies the binding onto the stick (bound player only), under tag `GTMQoLWirelessBinding`.
  - Right-click pastes it, only by that same player, onto machines that are unbound or already theirs.
  - Right-click with an empty stick binds an unbound machine to the clicker.

## Steam (`wireless/steam/`)

- `WirelessSteamSavedData` holds the pools (`BigInteger` per `NetworkId`) on the overworld, server thread only.
  - It also holds in-memory `IOStats` per network: cumulative inserted/extracted, sampled every 20 ticks by a server
    tick handler in `WirelessNetworks`. Rates are averaged over the last 10 samples (mB/t).
  - The stats are deliberately not saved (the user agreed). `moveAll` doesn't count as IO.
- `WirelessSteamTank` is a `CustomFluidTank` with no contents of its own; every read and write goes to the pool.
  It is wrapped in a normal `NotifiableFluidTank`, so GTCEu's recipe, pipe and steam multiblock code works unchanged.
  The view is int-capped (Forge tanks are int-sized).
- Machines (`WirelessSteamMachines`, bronze theme):
  - `wireless_steam_input_hatch` (`PartAbility.STEAM`) and `wireless_steam_output_hatch` (`EXPORT_FLUIDS`), both
    `WirelessSteamHatchPartMachine`.
  - `wireless_steam_accessor`: standalone block, fluid handler on all sides, optional auto output to the front.
  - `wireless_steam_monitor`: shows stored steam and input/output/net rates.
- Shared UI: `WirelessSteamUI.create(syncManager, binding, player)` shows the bound player, network, stored steam,
  Bind/Unbind, a Private toggle and "To team". The monitor extends it with the rates.
- Textures:
  - Both hatches use only `gtmqol:block/overlay/machine/overlay_wireless`, via
    `.colorOverlaySteamHullModel(WIRELESS_OVERLAY)` with no pipe or emissive layer, so input and output look
    identical. It is the animated rainbow swirl from GTMThings' `overlay_energy_on_wireless`, originally from GTNH.
  - The accessor uses GTCEu's `steam_hatch` model; the monitor uses GTCEu's screen overlay.
  - Credits are in `README.md`.

## EU (`wireless/energy/`)

The user chose concrete code alongside steam rather than a generic per-resource storage. `WirelessEnergySavedData`
has the same shape as the steam one. `FTBTeamsCompat`, `movePrivateToTeam` and `WirelessNetworks.onServerTick`
each call both. `IOStats` lives in `wireless/` and is shared.

- Machines (`WirelessEnergyMachines`): one per tier in `GTMachineUtils.ALL_TIERS` (ULV..UHV, or ..MAX with GTCEu's
  high-tier config), with names like `lv_wireless_energy_input_hatch`.
  - Input hatch: `INPUT_ENERGY`, `SUBSTATION_INPUT_ENERGY`, `INPUT_LASER`. Overclock toggle (tiers below MAX): one
    tier higher voltage, each EU costs 4 from the pool (16× energy for 4× voltage).
  - Output hatch: `OUTPUT_ENERGY`, `SUBSTATION_OUTPUT_ENERGY`, `OUTPUT_LASER`. No overclock.
  - Accessor: the front emits at its tier. Other sides accept any voltage (the user's choice), still limited to its
    amperage per tick. Always ticking.
  - `wireless_energy_monitor`: a single machine (LV hull) showing EU stored and rates in EU/t.
- Amperage per machine is 1..`MAX_AMPERAGE` (2^24, the user's choice), default 4 (`DEFAULT_AMPERAGE`).
  - UI: a text field plus ×/÷ buttons (plain 4, Shift 16, Ctrl 2), `WirelessEnergyUI.amperageRow`.
  - Per-player default: `/gtmqol default_amperage [amperage]` (`DefaultAmperageCommand`, any player, for
    themselves), saved in `WirelessEnergySavedData`. `WirelessNetworks.onEntityPlace` applies it to the hatches and
    accessors that player places; other placers get 4.
- The hatch Save button is ModularUI's `GuiTextures.SAVE` (floppy) icon, with the label in the tooltip.
- `WirelessEnergyContainer` (hatches) reports a **per-tick budget** of V × A as stored/capacity, not the pool. The
  power substation drains `getEnergyStored()` every tick, and `EnergyContainerList` sums all hatches, so reporting
  the pool would count it once per hatch.
- Hatch settings (amperage, overclock) are edited in the UI and applied by the Save button or on UI close
  (`applySettings`).
  - Multiblocks snapshot voltage/amperage/tier when they form. So applying re-forms every formed controller the
    GTCEu way (`checkStructurePattern` hits the cache, then `formStructure`), as `PatternState.onBlockStateChanged`
    does. The user approved this.
  - The accessor's amperage applies immediately.
- `WirelessEnergyContainer` overrides `getTotalContentAmount()` to return `getEnergyStored()`. The base returns its
  raw `energyStored` field (always 0 here), and `RecipeHandlerList.handleRecipe` skips input handlers whose total is
  0. Without the override, recipes report insufficient inputs.
- Both wireless containers (hatch and accessor) override `getContents()` to return `new EnergyStack(stored)`.
  - The base goes through `EnergyContainerList.calculateVoltageAmperage`. Its `hasPrimeFactorGreaterThanTwo` is
    linear in the amperage, which is millions of iterations at 2^24 A, on every parallel calculation (~77% of a
    server profile, 2026-10-05).
  - The same function is also fixed by `gtceufix/EnergyContainerListMixin` (1.21).
- Known limitation: GTCEu sums hatches in longs. One MAX hatch at full amperage is 2^55 EU/t, so it takes about 256
  of them on one multiblock to overflow.
- The EU UI reuses the steam binding lang keys (registered in `WirelessSteamMachines`).

## Covers (`wireless/WirelessCovers.java`)

- Registered from GTCEu's cover `RegisterEvent` (`GTMQoL.onRegisterCovers` on 1.20; declared in the constructor on
  1.21). The cover items are made in the same call through our registrate.
- `WirelessEnergyCover`: input and output, one per tier (`GTMachineUtils.ALL_TIERS`). Ids look like
  `gtmqol:wireless_energy_input.lv`, items like `lv_wireless_energy_input_cover`.
  - Moves up to V × A EU/t with `changeEnergy`, straight into or out of the machine's buffer. So the machine's
    voltage doesn't matter and nothing explodes.
  - Input needs `getInputVoltage() > 0` to attach, output `getOutputVoltage() > 0`.
  - Amperage UI and per-player default work as for the hatches.
- `WirelessSteamCover`: input fills the machine's fluid handler with network steam. Output drains steam (boilers)
  into the network, as much as the machine takes or gives per tick.
- Binding is the placing player (`onAttached`).
  - Covers have no machine, so they hold a `WirelessBindingTrait` built with the
    `(ISyncManaged owner, BooleanSupplier isRemote)` constructor as a sync field, like GTCEu's `FilterHandler`.
    `MachineTrait` methods that need a machine would throw.
  - No data stick support on covers.
- All four covers use the rainbow `overlay_wireless` texture for the cover and the item, so they look the same.

## EU ↔ FE (`fe/FEInputProvider.java`)

The user wanted every EU input to accept FE and every EU output to give FE. Toggle `misc.feInput`.

- FE → EU: a capability provider on every `MetaMachine` and cable. It exposes FE on the sides where the machine or
  cable takes EU, converting at GTCEu's `feToEuRatio`.
  - On 1.21 it registers in `RegisterCapabilitiesEvent`; on 1.20, `AttachCapabilitiesEvent`.
  - Packets are at most the input voltage, so nothing explodes.
  - It looks up the container without attached caps. Otherwise it and GTCEu's `EUToFEProvider` would query each
    other forever.
- EU → FE: nothing to do. GTCEu's `EUToFEProvider` (`nativeEUToFE`, default on) gives FE blocks an EU capability,
  so outputs and cables push into them. Pull-based FE pipes can't extract from GT machines.
