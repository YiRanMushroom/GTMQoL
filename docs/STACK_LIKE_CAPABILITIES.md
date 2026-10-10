# Stack-like recipe capabilities

1.21.1 only (`port/1.21.1`). Mekanism chemicals are the only type so far. Recipes, parts, AE and the pattern buffer
were tested in game (2026-10-10). The EMI real slots were not built yet when this was written.

A stack-like type is any fluid-like resource: a type plus a `long` amount, with ingredients that may match several
types (tags). One generic recipe capability, handler and display path covers every such type. A new type only
describes its stacks.

## Rules (the user's)

- Whatever enters a recipe has a real `RecipeCapability`. There is no capability for AE keys as such, so other
  mods' hatches can implement the same capability.
- AE support is opt-in per capability, through a bridge (capability → AE key).
- Dedicated capabilities (items, fluids) have priority over the universal parts.

## Pieces

### Core (`common/stacklike/`)

| Class | Role |
|---|---|
| `GenericStackLikeType<S, I>` | Describes a stack `S` and its ingredient `I`. Covers: id (also the cap id), classes, amounts, `copyWithAmount`/`withAmount`, `isSameType`, `lookupKey` (equal and same hash for the same type, used for lookup), `test`, `getStacks` (every stack an ingredient matches, at its amount), `of(stack)`, codecs, `displayName`. Ingredients are treated as immutable. |
| `GenericStackLikeRecipeCapability<S, I>` | `extends RecipeCapability<I>`. `register(type, color, sortIndex, name)` registers through our registrate under `GTRegistries.Keys.RECIPE_CAPABILITY`, adds the lang, calls `MapIngredientTypeManager.registerMapIngredient`, and adds the cap to `ALL`. Copying, multiplying, parallel limits and matching mirror gtceu's fluid capability. Has a `Serializer` record. `describe()` gives e.g. "1,000 x Oxygen". |
| `GenericStackLikeMapIngredient` | Recipe lookup node holding `lookupKey`. A tag ingredient becomes one node per matched type. |
| `GenericStackLikeNotifiableHandler<S, I>` | `extends NotifiableRecipeHandlerTrait<I>`, long amounts. Abstract `getStacks`, `extract` and `insert`. Matching is in `handleRecipeInner`; consumed inputs are recorded through `getRecipeMachine()`. Parts subclass it. |
| `mekanism/ChemicalStackLike` | `TYPE` and `CAP` (id `gtmqol:chemical`, sort index 4 between gtceu's cwu (3) and block_state (5), color `0xFF9C5ED6`). `init()` runs in the `GTMQoL` constructor when `mekanism` is loaded. Only touch it then. |

Use it in a recipe builder with `builder.input(CAP, ingredient)` / `builder.output(CAP, ingredient)`. Slot counts
go through `GTRecipeTypeBuilder.setMaxSize(IO, CAP, n)`.

### AE (`integration/ae2/`)

- `stacklike/AEStackLikeBridge<S, I>` has `cap()`, `isKey(AEKey)`, `toKey(S)` and `fromKey(AEKey, amount)`.
  `AEStackLikeBridges.register(...)` must run in the `GTMQoL` constructor, before any machine exists, because parts
  attach one handler per bridge in their constructor. Chemicals: `AppMekChemicalBridge` (Applied Mekanistics'
  `MekanismKey`), registered when `mekanism` and `appmek` are loaded.
- `MEUniversalInputPartMachine` is a stocking input with 36 scrolling slots, each holding any key the part can
  feed: items, fluids, or the keys of any bridge. The slot list is the source of truth (it is also the item
  handler). The fluid tank and the per-bridge handlers are views of the slots holding their keys. Auto pull, min
  stack size and ticks per cycle are shared. The config is `UniversalConfigWidget`, drawn by AE's key renderers.
  Recipe viewer drags go through AE's EMI converters. `MEStockingBus/HatchPartMachineMixin` extend the stocking
  parts' duplicate check to the universal inputs.
- `MEUniversalOutputPartMachine` is the dual output plus one handler per bridge. These handlers write into the
  non-item buffer, which is already flushed, saved and shown by key. `AEKeyDisplayWidget` draws any key type.
- Both parts carry the item/fluid import or export abilities. That is why gregified Mek multiblocks use
  `MultiblockShape.ANY_PARTS`.

### Pattern buffer (both buffers, always applied with AE2)

`integration/ae2/stacklike/PatternBufferStackLike` plus mixins in `core/mixins/ae2/`:

- `MEPatternBufferStackLikeSlotMixin` puts a `PatternBufferStackLikeSlot` on each `InternalSlot`: an
  `Object2LongOpenHashMap<AEKey>` of bridged keys. Gtceu's `add` ignores keys that aren't items or fluids, so the
  mixin takes them first. It covers `isEmpty`, refund and NBT (`gtmqol:stack_like`).
- `MEPatternBufferRecipeHandlerListMixin`: `BufferRecipeHandlerList.handlersFor` returns a per-slot handler for
  bridged caps. These are unattached `GenericStackLikeNotifiableHandler`s, like gtceu's slot handlers. A slot
  holding only bridged keys is not skipped as empty.
- `MEPatternBufferStackLikeMixin` (`PatternBufferStackLikeHolder`):
  - `checkInput` / `couldSlotMatchContents` accept bridged keys;
  - adds one attached aggregate handler per bridge, for lookup and parallel.
- `MEPatternBufferProxyStackLikeMixin`: proxies get forwarding handlers of the aggregates.
- `PatternBufferReturn` inserts bridged outputs too. `recipedb/PatternBufferIngredients` splits them per slot.
- `workers`, `Worker` and `Worker.slot` are public through our AT (gtceu is `accessCompileOnly` + `runtimeOnly`).
- Unclean points, accepted:
  - unattached traits;
  - `@Shadow this$0` (javac hides synthetic members, so an AT can't expose it);
  - the aggregate's parallel limit sums all slots, as gtceu does for items.

### Recipe viewer display (EMI)

How gtceu draws a recipe: `GTRecipeTypeUILayout.Builder` holds a layout builder and a content builder per
capability. Layout builders place named slot widgets. Content builders fill them per recipe
(`GTRecipeViewerWidget.capabilityWidgetName(cap, io, i)` links the two). Gtceu only sets defaults for items, fluids,
EU and CWU.

- `core/mixins/GTRecipeTypeUILayoutBuilderMixin` (TAIL of the builder's `<init>`) calls
  `StackLikeRecipeViewer.addDefaults` for every cap in `GenericStackLikeRecipeCapability.ALL`. A type's own `UI(...)`
  can still override them.
- `StackLikeRecipeViewer` copies gtceu's FLUID builders:
  - Layout: 1 slot is a single slot. More slots use
    `SlotGroupWidget.builder().matrix(capabilityInfo(cap).getRecipeViewerGrid(io))`.
  - Each slot is `RecipeViewerSlotWidget.create(stackClass)`, holding a `StackLikeEntryList`, with the fluid slot
    background and the cap's overlay.
  - Content: the entry list from `type.getStacks(ingredient)`; overlays `ContentOverlay` (chance/tick marks) and
    `StackLikeAmountOverlay`; the chance; the per-tick tooltip line; the role (CATALYST for chance 0 inputs).
- MUI's `RecipeViewerSlotWidget.create(cls)` gives an `EmiRecipeViewerSlot` in EMI. It wraps a real EMI
  `SlotWidget` (`TankWidget` for `FluidStack`), so hover, tooltip, R/U and favorites all come from EMI. It converts
  through `EmiStackConverter.register(Class, Converter)`.
  - `mekanism/ChemicalEmiConverter` builds stacks with Mekanism's `IMekanismEmiHelper`
    (`IMekanismAccess.INSTANCE.emiHelper()`). It is registered in `GTMQoLClient.init` when EMI and Mekanism are
    loaded.
  - A cap without a converter shows empty slots.
- `core/mixins/GTEmiRecipeStackLikeMixin` (client; `@ModifyReturnValue` on `getInputs` / `getOutputs`) appends
  stack-like contents through `StackLikeEmi`, because `GTEmiRecipe` only lists items and fluids. EMI's recipe lookup
  (U on a chemical finding GT recipes) and AE2's pattern encoding (`EmiStackHelper.ofInputs` → `getInputs`; AppMek
  has an EMI chemical converter) read these lists. Outputs are the first stack with the chance, like gtceu's.

Gotchas:

- `value(...)` resets the background to `SLOT_ITEM` for every type except fluids. Set `background(...)` after it.
- The `chance` setter doesn't rebuild the EMI slot; `recipeSlotRole` does. Set the role last.
- `ContentOverlay` only draws amounts for `SizedFluidIngredient`, hence `StackLikeAmountOverlay`. It mirrors
  gtceu's `drawFluidAmount`: `FormattingUtil.formatBuckets`, 0.5 scale, z+400, falling back to
  `formatNumberReadable` when wider than 32 px.

### Jade

`integration/jade/GTMQoLJadePlugin` (`@WailaPlugin`; Jade is `compileOnly` + `localRuntime`) registers
`StackLikeRecipeOutputProvider`. Its priority is `BODY + 1`, so it runs after gtceu's `RecipeOutputProvider`. It
covers outputs only, like gtceu. When the recipe has no item or fluid outputs, it adds gtceu's "Recipe Outputs:"
line itself. Names come from `displayName` and `describe`.

### Tooltips

Machine tooltip lang keys must not be `<ns>.machine.<id>.tooltip`. `MetaMachineBlock.appendHoverText` adds that
key by itself, so it showed twice. Ours end in `.desc`.

## Adding a stack-like type

1. Implement `GenericStackLikeType<S, I>` (see `ChemicalStackLike`) and keep it in a class touched only when the mod
   is loaded.
2. In the `GTMQoL` constructor, behind `ModList.get().isLoaded(...)`: `CAP = GenericStackLikeRecipeCapability.register(TYPE, color, sortIndex, name)`.
3. A part or machine holding it: subclass `GenericStackLikeNotifiableHandler`.
4. AE (optional): an `AEStackLikeBridge` for the mod that puts the type into ME networks. Register it in the
   constructor before the machines. The universal parts and the pattern buffer then take it with no further
   changes.
5. EMI (optional): an `EmiStackConverter.Converter<S>`, registered in `GTMQoLClient.init` behind EMI + the mod.
   Without it the slots stay empty and EMI lookup and AE encoding skip the type.
6. Recipe types using it: `setMaxSize(IO, CAP, n)` (gregification: `ForeignMachineType.withRecipeType`).

## Where to look

- gtceu 1.21 sources, unpacked in `.gtceu-src-1.21/com/gregtechceu/gtceu/`:
  - `api/recipe/gui/{CapabilityContentBuilder,RecipeViewerCapabilityLayoutBuilder,ContentOverlay,GTRecipeTypeUILayout}.java`;
  - `integration/recipeviewer/emi/recipe/GTEmiRecipe.java`;
  - `api/capability/recipe/FluidRecipeCapability.java` (the capability we mirror);
  - `api/machine/trait/notifiable/NotifiableFluidTank.java` (the handler we mirror);
  - `integration/ae2/machine/MEPatternBufferPartMachine.java`.
- ModularUI sources (`.mui-src-1.21/brachy/modularui/integration/`):
  - `emi/EmiRecipeViewerSlot`, `emi/EmiStackConverter`;
  - `recipeviewer/RecipeViewerSlotWidget`, `recipeviewer/entry/EntryList`.
- Mekanism sources jar, under Gradle's `modules-2` cache at `mekanism/Mekanism/1.21.1-10.7.19.85/`. Read it with
  PowerShell `System.IO.Compression.ZipFile`. It has `mekanism/api/chemical`, `mekanism/api/recipes/*` and
  `mekanism/api/integration/emi/IMekanismEmiHelper`.
- Applied Mekanistics jar (Modrinth `TpUCzFaW`, cache `maven.modrinth/applied-mekanistics`): `MekanismKey` and its
  EMI converter.
- AE2 sources jar 19.2.18: `AEKey`, `EmiStackHelper`.
