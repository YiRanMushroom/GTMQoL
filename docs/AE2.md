# AE2 integration (`integration/ae2/`, `core/mixins/ae2/`, `core/mixins/eap/`)

Split out of `context.md`. Everything here only runs when `GTCEu.Mods.isAE2Loaded()`. Chemicals and other stack-like
types in ME parts and the pattern buffer are in `STACK_LIKE_CAPABILITIES.md`.

## Overclocked pattern buffer

- `AbstractMEPatternBufferPartMachine extends MEPatternBufferPartMachine` adds `getPatternColumns()` /
  `getPatternRows()`. These must return constants, because the superclass constructor calls them.
- GTCEu's `MAX_PATTERN_COUNT = 27` is inlined. `MEPatternBufferPartMachineMixin` `@ModifyConstant`s the five 27s in
  `<init>` (pattern inventory, internal slots, pattern details), `syncWorkerCount` and `addWorker` (`require = 5`;
  recheck after a GTCEu bump).
- `getTerminalPatternInventory` and `buildMainUI` (scrolls past 6 rows) are overridden instead.
- The shared inventory and tank stay at GTCEu's 9 slots. The old 25-slot catalyst version would need the whole
  `getPanelBuilder`.
- Unformed, the pattern terminal shows GTCEu's buffer icon (`getTerminalGroup`; `customName` is private).
- `AE2Machines`: `gtmqol:overclocked_me_pattern_buffer`, 12 columns × 18 rows = 216, LuV. Its recipe is in the
  magical assembler: 4 ME pattern buffers, 16 MV circuits, circuit 24, 576 soldering alloy, 4000 glue, 1200 t, MV.
  This matches the old `QoLMachines.kt`.

## ME processing (`AEProcessing`, port of the v7 ME machines)

- Recipe types and SimpleTieredMachines: `gtmqol:me_assembler` (6/1/3/0) and `gtmqol:me_circuit_slicer`
  (1/1/0/0), plus the four silicon chip items.
- The v7 recipes:
  - machine crafting, with an AE2 inscriber in the middle;
  - wafer → chips (8/16/32/64);
  - AE2 materials → prints;
  - chip + print + silicon print (or 4 copper foil) + 144 redstone → processors × the chip multiplier;
  - GTCEu ME buses/hatches/pattern buffer (+ proxy) from AE2 parts;
  - wiremill/polarizer/mixer AE recipes.
- AE integration-mod recipes are 1.21 only.
- Machine names come from `SimpleMachineBuilder`'s `toEnglishName`, so they read "Me Assembler".

## Smart doubling (ExtendedAE Plus, optional)

- `gtmqol.eap.mixins.json`, gated by `core/EAPMixinPlugin` (applies when `LoadingModList` has `extendedae_plus`).
- `eap/MEPatternBufferSmartDoublingMixin` targets GTCEu's buffer, so ours too.
  - It implements `ISmartDoublingHolder` with a `@SaveField` toggle (default on) and a limit (0 = none).
  - It copies both onto the slot patterns (`ISmartDoublingAwarePattern`, `PatternScaler.getComputedMul`), at
    `getAvailablePatterns` HEAD and in the setters.
- `pushPattern` unwraps `ScaledProcessingPattern` to its original for the slot/worker matching, and for
  `worker.pattern` so refunds still match. It still pushes the scaled pattern's inputs (`@ModifyArg` on both
  `InternalSlot.pushPattern` calls). The user said this unwrap is required.
- UI: a "×2" left configurator opens a popup (toggle + limit). It is added by `@ModifyReturnValue` on
  `getPanelBuilder` (`SmartDoubling.addConfigurator`). Lang keys are registered in `AE2Machines`.
- On 1.21 the NeoForge EAP's `ISmartDoublingHolder` only has the limit, so the on/off switch is our own mixin
  method.

## ME dual and universal parts (`AEDualParts`, LuV)

- `me_dual_input` extends the stocking bus and adds a stocking fluid list.
  - The main UI has items on the left and fluids on the right.
  - `FluidConfigWidget` is a trimmed copy of GTCEu's `AEConfigWidget` with prefixed sync names. The original's
    fixed action names collide when two widgets sit in one panel. Resync it if GTCEu changes the widget.
  - The settings popup has item and fluid columns, with separate min stack size / ticks per cycle for fluids.
  - The fluid list must be `attachPersistentTrait(name, trait)`; with plain `attachTrait` it was lost on reload.
- `me_dual_output` extends the output bus and adds a fluid buffer.
  - autoIO inserts into the network every tick instead of every `updateIntervals`.
  - `buildMainUI` copies the output bus's. Its waiting list syncs a `KeyStorage` refilled from both buffers, since
    the item buffer is private.
- Universal input/output (1.21): 36-slot scrolling input of any bridged key, and an output with per-bridge
  handlers. See `STACK_LIKE_CAPABILITIES.md`.
- Recipes: ME assembler (or the assembler if `ae2.processing` is off). Inputs are stocking bus + stocking hatch, or
  output bus + output hatch, plus 1000 mB glue and 144 mB (1 L) soldering alloy.
- Data sticks share the "MEInputBus" key. Fluids only paste between duals.
- Tooltip keys end in `.desc` (`STACK_LIKE_CAPABILITIES.md`, Tooltips).

## Pattern buffer return (`PatternBufferReturn`, `MEPatternBufferReturnMixin`)

- An extra OUT `RecipeHandlerList` (HIGHEST priority, undyed) is added to `getRecipeHandlers` once and cached;
  identity matters.
- It inserts every output of the multiblock into the network, and AE2 hands them to waiting crafting CPUs. If the
  network refuses, the other output parts get it.
- Toggle per buffer: left configurator "ME", default on. Global switch `ae2.patternBufferReturn`, read at runtime.

## Sticky card and universal circuit encoding

Ported from the v7 code in `reference/`.

- `gtmqol.ae2.mixins.json`, gated by `core/AE2MixinPlugin`. It needs AE2; the ExtendedAE mixin also needs
  `extendedae`. ExtendedAE's mod id is `extendedae`; v7's 1.20 fork used `expatternprovider`.
- Mixins in `core/mixins/ae2`:
  - `EncodingHelperMixin`: `@ModifyExpressionValue` on the `Comparator.comparing` in `<clinit>`. It adds "is a
    universal circuit" after craftable, so patterns encode with the universal circuit.
  - `MEInventoryHandlerMixin` adds `ISticky`: a flag, plus `shouldStick` = sticky, non-empty partition, and the
    partition passes the key.
  - `NetworkStorageMixin`: a `@Share` stop flag, set after an insert into a sticky handler that has the key
    partitioned. Wrapped iterators of `priorityInventory.values()` and `secondPassInventories` stop on it.
  - `StorageBusPartMixin` / `PartSpecialStorageBusMixin` (ExtendedAE's mod / precise / tag buses) set the flag from
    `isUpgradedWith(STICKY_CARD)` at the `setVoidOverflow` call in `updateTarget`.
- `StickyCardItem`:
  - the item, its tooltip lang, and `Upgrades.add` in `FMLCommonSetupEvent`;
  - recipes: the ME assembler if `ae2.processing`, plus the shaped one;
  - no tooltip group for the Upgrades (AE2's storage bus upgrades have none).
- Early switches in `config/gtmqol-early.properties` (`EarlyConfig`):
  - `ae2.universalCircuitEncoding`: only `EncodingHelperMixin`.
  - `ae2.stickyCard`: the other four mixins, the item and its recipes. These mixins also check
    `StickyCardItem.STICKY_CARD != null`.
- `StorageBusPartMixin` `@Shadow`s the `handler` field, whose type `StorageBusInventory` is private.
  `META-INF/accesstransformer.cfg` opens it, but MDG only applies ATs to Minecraft. So the ModAccessor plugin
  (`dev.vfyjxf.modaccessor`, build time only) patches the AE2 jar on `accessCompileOnly` for javac. A
  `@WrapOperation` with the supertype receiver did not match. ExtendedAE is `compileOnly` too.

## Pattern encoding skips non-consumed inputs

- Toggle: toma `ae2.skipNotConsumedInputs` (runtime, default on).
- `EmiEncodePatternHandlerMixin` (client, EMI only) wraps `EmiStackHelper.ofInputs`. It blanks inputs whose
  `EmiIngredient.getChance() == 0`; GT's `GTEmiRecipe` passes content chance through. AE2 skips empty input lists.
- AE2 19 has no JEI module, so 1.21 is EMI only. 1.20.1 has a JEI mixin too.

## Known

- 1.20.1: GTCEu's JEI mixins constrain the versions. JEI stays 15.20.0.115, so EAP stays 1.6.1.
- A JVM access violation (C2 JIT, `InventoryChangeTrigger`) happened once while picking up a buffer. Ignored.
