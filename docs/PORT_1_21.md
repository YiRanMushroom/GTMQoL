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
- 1.21 only features: gregification (`GREGIFICATION.md`), stack-like capabilities
  (`STACK_LIKE_CAPABILITIES.md`), the universal ME parts, AE integration-mod recipes.

## Gtceu bug workarounds (`core/mixins/gtceufix/`)

Delete each one once upstream fixes it. All are 1.21 only, except `BaseSchemaRendererMixin`.

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
- `gtmqol.jeifix.mixins.json` (`gtceufix/jei/JeiRecipeSlotMixin`, gated by `core/JeiRecipeSlotFixPlugin`): JEI
  19.46+ dropped the `RecipeSlot.allIngredients/displayIngredients` fields (replaced by `RecipeSlotIngredients`).
  ModularUI's `jei.RecipeSlotAccessor`, in a required config, still targets them. So `RecipeSlot` failed to load,
  and every recipe that goes through JEMI in EMI broke (ATM10's JEI 19.57; Create showed nothing).
  - The mixin adds the two fields back (priority 500, before MUI's accessor). JEI doesn't read them, and MUI only
    writes them for its own JEI categories.
  - The plugin applies it only when JEI is present and the fields are missing. It reads the class through
    MixinService's bytecode provider.
