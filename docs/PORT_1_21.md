# 1.21.1 port (branch `port/1.21.1`)

Split out of `context.md`. Build setup and dependency rules are in `CLAUDE.md`.

- Branched from `87a49de` (GTNL steam machines). The plan is two long-lived branches kept in sync with
  cherry-picks. A multi-version tool such as Stonecutter is only an idea for now and has not been agreed.
- Build setup:
  - ModDevGradle 2.0.141 on NeoForge 21.1.248 with Java 21.
  - Dev-only mods go in a `localRuntime` configuration.
  - Mixin configs are listed in `neoforge.mods.toml`.
  - Mixin `compatibilityLevel` is `JAVA_21` and `pack_format` is 34.
  - Versions follow gtceu's 1.21 branch (`gradle/libs.versions.toml`, `forge.versions.toml`).
- `src/generated/resources` still has the 1.20 layout (`data/forge/tags/items`). Regenerate it with `runData`.

## Code differences from 1.20.1

Keep these in mind when cherry-picking.

- Gtceu 1.21 registers everything as deferred Registrate entries. Machine, cover and recipe type fields are
  `MachineEntry` / `Holder<CoverDefinition>` / `GTRecipeTypeEntry`, not definitions. Content is declared in the
  `GTMQoL` constructor, recipe types first. `IGTAddon` has no `initializeAddon`.
- Builders are in `api.registry.registrate.builder`. `register()` returns `MachineEntry`, and the properties live
  behind `properties()`. `RuntimeGeneration` keeps both the builder and the entry.
- `ModularMachines`: `MachineBuilderMixin` only queues the entry and the builder's recipe type suppliers (read
  through `MachineBuilderAccessor`). The multiblocks are declared at the start of the `gtceu:machine`
  `RegisterEvent`, at NORMAL priority. That is before our registrate's LOW `onRegister` and after the recipe types
  exist. The config is readable by then.
- `FEInputProvider` registers `Capabilities.EnergyStorage.BLOCK` in `RegisterCapabilitiesEvent` for every
  `MetaMachineBlock` / `CableBlock`. Gtceu registers first, so its own FE storages win.
- Data stick binding uses `DataComponents.CUSTOM_DATA` / `CUSTOM_NAME`. SavedData uses `SavedData.Factory` plus
  `HolderLookup.Provider`.
- `CircuitTags` uses `c:` (Mekanism 1.21: `c:circuits/<tier>`, `c:alloys/advanced`).
- The dev recipe viewer is EMI, not JEI, matching gtceu's 1.21 dev setup. With JEI, this gtceu snapshot crashes in
  `GTRecipeCategories.<clinit>`: `CategoryIcon` calls `GTJEIPlugin.getRuntime()` during mod construction, and it is
  still null there.
- EAP is pinned by Modrinth version id, because the Forge and NeoForge builds share version numbers. The NeoForge
  EAP's `ISmartDoublingHolder` only has the limit, so the smart doubling on/off switch is our own mixin method,
  passed to `SmartDoubling.addConfigurator`.
- The AE2 dev runtime is 19.2.18, because EAP 1.6.3 needs `AEBaseMenu.clicked`. Gtceu builds against 19.2.8.
- The mixin configs' `mixinextras.minVersion` is 0.5.3, the version NeoForge 21.1.248 bundles. It is 0.5.5 on
  1.20.1.
- `GTMQoLAddon` calls `defaultCreativeTab((ResourceKey) null)`, as gtceu's `GTRegistration` does. Registrate's
  default tab is SEARCH, so otherwise every item adds itself to it a second time and NeoForge throws
  `already exists in the tab's list`.
- `KubeJSDataGenFix` is removed, since NeoForge's `DatagenModLoader` sets the flag correctly.
- Auto-build (`common/autobuild/`): 1.20.1 uses `FriendlyByteBuf` (blocks via `writeId`/`readById` on
  `BuiltInRegistries.BLOCK`), Forge 47 events (`TickEvent.ServerTickEvent` END phase, `ServerStoppedEvent`,
  `ForgeEventFactory.onBlockPlace`). The 1.20.1 gtceu snapshot's `MultiPredicate` has no `getParent()`, so
  `StructurePlanner` keeps a node → root `IdentityHashMap`. For block patterns it comes from `getPredicates()`. For
  expandable patterns it takes one extra plain `populate` pass and asks `getPredicateFromPos` for every position.
- Written on 1.21 first: gregification (`GREGIFICATION.md`), stack-like capabilities
  (`STACK_LIKE_CAPABILITIES.md`), the universal ME parts, Jade, the potion features (`POTIONS.md`). The 1.20.1
  sync state is in `context.md`.

## Gtceu bug workarounds (`core/mixins/gtceufix/`)

Delete each one once upstream fixes it. All are 1.21 only, except `BaseSchemaRendererMixin`,
`AbstractWidgetMenuMixin` and `GTEmiRecipeMixin`. The canner's potion representative recipes (`CannerLogicMixin`, a
feature, not in `gtceufix/`) are on both branches too.

- `EmiCallWrapperMixin`: clicking the recipe type button in a machine UI makes gtceu call the private
  `EmiApi.setPages` through `EmiApiAccessor`. That invoker is never applied to `EmiApi`, so the click throws
  `NoSuchMethodError`. The mixin switches the call to the public `EmiApi.displayRecipeCategory(machineCategory)`.
  That no longer shows the recipe type's other categories as tabs. Because of this, EMI is `compileOnly`.
- `GameDataMixin`: `GTRegistries` adds `gtceu:recipe_type` to the load order, and gtceu's own `GameDataMixin` maps
  it to the vanilla recipe type registry. So vanilla `recipe_type` gets posted a second time, under a gtceu key,
  only to make KubeJS registration easier. Mods that handle the event by `event.getRegistry()` (Core Lib,
  CyclopsCore) then throw duplicate-registration errors, and ATM10 fails to load.
  - The mixin WrapOperations `ModLoader.postEventWrapContainerInModOrder` so the duplicate event only goes to gtceu
    and kubejs. It's a WrapOperation because ModernFix @Redirects the same call.
  - Gtceu also moves its registries and `recipe_type` ahead of `attribute`. CyclopsCore assumes ATTRIBUTE is the
    first event and queues every entry there. It then throws `Tried registering ... after its registration event`,
    and the cascade leaves `neoforge:swim_speed` unbound. `getRegistrationOrder` is `@ModifyReturnValue`d to put
    ATTRIBUTE back at the front.
- `GTMuiWidgetsMixin`: in `GTMuiWidgets.createCircuitSlotSyncValue` the setter calls
  `IntCircuitBehaviour.stack(v, current.getCount())`. When the slot is empty the count is 0, which gives an empty
  stack, so the ghost circuit can never be set. The mixin clamps the count to ≥1. 1.20 uses `stack(v)`.
- `GTRecipeTypeMixin`: `GTRecipeTypeBuilder.addCustomRecipeLogic` only adds to `GTRecipeType.Properties`, and the
  constructor never copies that list into the type. So no custom recipe logic runs: forming press naming with a
  named casting mold, rotor recycling in the macerator/arc furnace, canner, brewery, data stick copying. Their
  representative recipes are missing from EMI too. The mixin copies the list at the end of the constructor.
  1.20.1's `addCustomRecipeLogic` adds to the type directly, so it isn't affected.
- `GTRecipeBuilderMixin`: `GTRecipeBuilder.outputFluids(FluidStack)` uses NeoForge's `SizedFluidIngredient.of`, which
  keeps only the fluid (`FluidIngredient.single(stack.getFluid())`). Output data components are dropped. The
  brewery's potion outputs come out as a potion fluid with no contents, and EMI can't look up the recipe from a
  specific potion. For stacks with components, the mixin uses a strict `DataComponentFluidIngredient`. Inputs go
  through `RecipeHelper.makeFluidIngredient` and keep their components. 1.20.1's gtceu `FluidIngredient.of` keeps the
  NBT.
- `GTEmiRecipeMixin` (`client`, **both branches**; 1.20.1 keeps the NBT): `GTEmiRecipe.getOutputs` builds fluid outputs with `EmiStack.of(fluid, amount)`, again
  dropping components. EMI looks recipes up (R / click) by `getOutputs`, so a potion fluid showed its uses (U, via
  `getInputs` and ModularUI's `EmiStackConverter`) but no recipes. The mixin passes the components through.
- `AbstractWidgetMenuMixin` (ModularUI bug): a menu button's menu panel stays open after the button is disposed, and
  the next click crashes in `MenuPanel.closeAllMenus` with `ContextMenuButton#<name> is not in a valid state`. GTCEu
  hits it with the structure error list in `GTMultiblockTextUtil.addUnformedWarning`: a `DynamicWidget` rebuilt
  whenever the errors change, with a hover-opened `ContextMenuButton` per missing part ability (e.g.
  `export_fluids`). Auto-build changes the errors while the UI is open. The mixin closes the menu at the head of
  `dispose`, while the button is still valid. Not verified in game yet.
- `BaseSchemaRendererMixin` (`client`, **both branches**) works around a ModularUI bug. `BaseSchemaRenderer.draw`
  computes the GL viewport from MUI's own `transformX/Y` only. The recipe UI that EMI/JEI embeds is translated
  through `PoseStack`, so the multiblock preview was drawn at the screen's top-left. The mixin WrapOperations
  `Viewport.calculateOpenGLViewportFromRectangle` and takes the origin from `context.getLastGraphicsPose()`. In a
  normal MUI screen the pose is exactly MUI's matrix, so nothing changes there.
- `GTRecipeCategoryMixin`: `GTRecipeCategory.getLanguageKey()` is `recipe_category.<ns>.<path>`, but Registrate
  only generates `recipe_type.<ns>.<path>`, and gtceu's lang has no `recipe_category.*`. So every category name
  showed as the raw key. For a default category (id == recipe type id) the mixin returns the recipe type's key.
  Extra categories (KubeJS, `GTRegistrate.recipeCategory`) are unchanged.
- `MEPatternBufferInternalSlotMixin`: a pattern buffer `InternalSlot` keys its contents by count-1 stacks
  (`AEItemKey.toStack()`, `AEFluidKey.toStack(1)`), with the real amount kept in a long. In 1.21,
  `handleItemInternal`/`handleFluidInternal` match with NeoForge's `SizedIngredient.test` /
  `SizedFluidIngredient.test`, which also require `count >= ingredient count`. So any ingredient needing more than
  1 failed, and with parallel even a one-in/one-out recipe showed "insufficient item". The parallel count
  (`getMaxByInput`) was right. The mixin tests only the item/fluid.
- `AEConfigSyncHandlerMixin` (`@WrapMethod`): the ME stocking / plain ME input bus config UI (and our dual input)
  didn't update while open. `AEConfigSyncHandler.detectAndSendChanges` computes the per-slot `changed` flags and
  updates its cache inside the `syncToClient` writer lambda, which runs at packet encode time. If encoding runs
  twice, the delivered packet has every flag false. The mixin resends everything when anything changed. Confirmed
  in game; the encode-twice cause itself is unverified.
- `EnergyContainerListMixin`: `EnergyContainerList.hasPrimeFactorGreaterThanTwo` trial-divides up to `l / 2`, so
  it is O(amperage). The multiblock UI's energy line rebuilds the list every tick, and with wireless hatches'
  amperage that was 77% of a profile. The mixin replaces it with "is not a power of two". Odd primes differ, but
  the caller ends at the same `amperage = 1` for them. See also `WIRELESS.md`.
- `RecipeRunnerMixin` (both branches): the OR / XOR chance logics compute `times * chance` as an int. Above about
  215k runs (parallel × batch, 100% output) it goes negative and chanced outputs are silently lost. The mixin wraps
  the `ChanceLogic.roll` call in `fillContentMatchList` and rolls in chunks that don't overflow. Then it merges the
  chunks' results: the same item or fluid is summed and split only at `Integer.MAX_VALUE`. Unmerged, every chunk
  added entries, and gtceu's `NotifiableItemStackHandler.handleRecipe` is quadratic in the number of entries, which
  cost about 10 mspt on the ME outputs. Ranged (IntProvider) outputs and other capabilities aren't merged. Not built yet.
- `RecipeLogicMixin` (both branches): when `findAndHandleRecipe` restarts the last recipe, it clears
  `lastOriginRecipe` and never sets it back. The next `onRecipeFinish` (`alwaysTryModifyRecipe`) then marks the recipe
  dirty and goes idle. Seen in the beehive log: a finish whose re-check failed on energy (our wireless hatch checked
  against what was left of the tick's budget, see `WIRELESS.md`) went idle, restarted through that path, and the next
  finish went idle again. The mixin keeps the origin (`@Share` from HEAD, restored after `setupRecipe` if it started). Not built yet.
- `gtmqol.jeifix.mixins.json` (`gtceufix/jei/JeiRecipeSlotMixin`, gated by `core/JeiRecipeSlotFixPlugin`): JEI
  19.46+ dropped the `RecipeSlot.allIngredients/displayIngredients` fields (replaced by `RecipeSlotIngredients`).
  ModularUI's `jei.RecipeSlotAccessor`, in a required config, still targets them. So `RecipeSlot` failed to load,
  and every recipe that goes through JEMI in EMI broke (ATM10's JEI 19.57; Create showed nothing).
  - The mixin adds the two fields back (priority 500, before MUI's accessor). JEI doesn't read them, and MUI only
    writes them for its own JEI categories.
  - The plugin applies it only when JEI is present and the fields are missing. It reads the class through
    MixinService's bytecode provider.
