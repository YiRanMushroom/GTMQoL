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
  Recipe viewer drags go through AE's EMI converters. The ME dual input is this part without the bridged keys
  (`AE2.md`). The duplicate check across every stocking part of a multiblock (gtceu's bus and hatch, our dual and
  universal inputs) is `StockingParts.configuredElsewhere`; `MEStockingBus/HatchPartMachineMixin` add it to gtceu's
  parts.
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

## 1.20.1 port (in progress)

Agreed with the user on 2026-10-10. Same design as above; only the differences are listed here.

Status: everything is written but not built yet: core, chemical cap, viewer mixins, JEMI converter, gregification
+ Mek machines, the AE bridge/universal parts/pattern buffer, and Jade.

Code differences from 1.21:
- Registration: `register` only creates the cap and its lang. `registerAll` is a `RecipeCapability`
  `GTCEuAPI.RegisterEvent` listener (`GTMQoL` constructor).
- No `registerMapIngredient`, because `getFrom` falls back to `getDefaultMapIngredient` for the interface content class.
- `GenericStackLikeType` has `writeIngredient` / `readIngredient` (`FriendlyByteBuf`) instead of a `StreamCodec`.
- JEI lookup: `StackLikeJei` + `GTRecipeJEICategoryStackLikeMixin`, which targets the full descriptor and not the
  generic bridge.
- Gregification (no MI on 1.20.1, only Mekanism):
  - Recipe types are registered with the same three `GTRegistries.register` calls as `GTRecipeTypes.register`, on
    GTCEu's recipe type `RegisterEvent`. `Gregification.init` must run before our own listeners so `ModularMachines`
    sees the gregified types.
  - GTCEu 1.20.1 converts proxied recipes in `RecipeManagerHandler.addProxyRecipesToLookup`, one GT recipe per
    foreign recipe. `RecipeManagerHandlerMixin` cancels it for `GregifiedRecipeType` only and adds `toGTRecipes`
    instead (rotary makes two).
  - Recipes are built with `buildRawRecipe` (no JSON round trip). Ids are `gtmqol:/gregification/...`, not in the
    recipe manager.
  - Dedicated server: the converted recipes aren't synced, so `GregificationClient` converts the synced foreign
    recipes on `RecipesUpdatedEvent` (HIGHEST, before the viewers reload) and puts them in the category map.
  - Mek machines use the 10.4 recipe classes and tick constants (`BASE_TICKS_REQUIRED` etc.); items and fluids go
    through Mek's `Single`/`Multi`/`Tagged` ingredient classes (not API, cast).
- Jade: same provider as 1.21, with plain `NbtOps.INSTANCE` (no registry ops). Jade is `modCompileOnly` now.
- AE (`integration/ae2/stacklike/`, universal parts, pattern buffer):
  - The bridge is registered at the top of the AE2 block in `GTMQoL.onRegisterMachines`, when Mek and appmek are
    loaded.
  - gtceu is `:slim`, so there is no access transformer for it: the gtceu mixins are `remap = false`, the
    package-private `MEPatternBufferPartMachine$Worker` is `@Coerce Object` + `MEPatternBufferWorkerSlotAccessor`,
    `$InternalSlot` reaches its buffer through `@Shadow this$0`.
  - 1.20.1 gtceu has no `getActionSource()`; `IActionSource.ofMachine(buffer.getMainNode()::getNode)` instead.
  - `UniversalConfigWidget` takes JEI drags (through AE2's `IngredientConverters`) as well as EMI ones.
  - `EncodePatternTransferHandlerMixin` counts the chemical slots in JEI encoding (assumes items, fluids, then one
    slot per stack-like content; falls back to keeping every input).
  - The universal parts' blockstates, models, lang and recipes need a `runData`.

### Chemicals on Mekanism 10.4

- Mek 10.4 has four chemical kinds (Gas, InfuseType, Pigment, Slurry), each with its own stack and ingredient
  classes. There is still **one** cap, `gtmqol:chemical`: `S = ChemicalStack<?>`, `I = ChemicalStackIngredient<?, ?>`.
- Dispatch is by Mek's own `ChemicalType` (`getTypeFor(stack / ingredient)`), the way Mek handles mixed chemicals:
  - ingredients: `IngredientCreatorAccess.getCreatorForType(type)` (`read`, `deserialize`, `from`);
  - stacks: `BoxedChemicalStack` (`box`, `read`, `write`) for NBT.
- `lookupKey` is the chemical object; Gas and InfuseType instances never collide.
- Codecs, unclean but accepted:
  - Mek 10.4 has no codecs. The ingredient codec wraps Mek's JSON (`serialize()` / `deserialize`) through
    `ExtraCodecs.JSON`, with an added `chemicalType` field (`ChemicalType.getSerializedName()`). Mek's JSON alone
    can't tell the kind of a tag ingredient.
  - The stack codec goes through `CompoundTag.CODEC` and `BoxedChemicalStack`.
  - Network: the `ChemicalType` ordinal, then Mek's `write(buf)` / `read(buf)`.
  - `withAmount`: Mek's API has no way to change an ingredient's amount, and ingredients are shared and immutable,
    so it builds a new one with Mek's creator from the parts of the old one:
    - single: `getInputRaw()` is used as is.
    - tag: the private `tag` field is read through `core/mixins/mekanism/TaggedChemicalStackIngredientAccessor`.
      `GTMQoLMixinPlugin` only applies `mekanism/` mixins when Mek is loaded.
    - multi: `IMultiIngredient.getIngredients()` is mapped recursively and joined with `creator.from(Stream)`. That
      method builds the array with the kind's own ingredient class; `createMulti` on a raw array would throw
      `ClassCastException`.

    Every element of a multi ingredient gets the same amount. `ingredientAmount` is the first representation's
    amount.
  - `test` checks that the `ChemicalType`s match before `testType`, because an ingredient of one kind may cast the
    stack to its own stack class.
  - `ChemicalStackLike` uses raw types and unchecked casts internally, because the four kinds share no common
    generic type that the cap can name.
- Applied Mekanistics 1.4.3 (Modrinth version `9n9p68Qq`): `MekanismKey` also wraps a `ChemicalStack<?>`, so there
  is one bridge.

### Recipe viewers

- JEI (the dev viewer):
  - Slots: MUI's `JeiRecipeViewerSlot` passes the entries to JEI's `addIngredientsUnsafe`, and JEI finds the type by
    class. Mek registers `GasStack`, `InfusionStack`, `PigmentStack` and `SlurryStack` as JEI ingredients, so the
    slots are real JEI slots (render, tooltip, R/U, bookmarks) with no converter.
  - Lookup: JEI indexes recipes through `GTRecipeJEICategory.setupRecipeIngredients`, which lists items and fluids
    only. A TAIL mixin adds the stack-like contents (the JEI counterpart of `GTEmiRecipeStackLikeMixin`).
  - AE2 encoding reads the JEI slots; AppMek's JEI converter handles chemicals.
- EMI: Mek 10.4 has no EMI integration (no `IMekanismEmiHelper`). Mek chemicals only exist in EMI through JEMI,
  EMI's built-in JEI plugin loader, so they need JEI installed.
  - When EMI is loaded, GTCEu skips its JEI categories and MUI picks the EMI slot handler, so GT recipes are
    `GTEmiRecipe`s with `EmiRecipeViewerSlot`s.
  - The converter wraps `dev.emi.emi.jemi.JemiUtil.getStack(chemicalStack)` (a `JemiStack` using Mek's JEI
    renderer and helper), then sets the amount. `convertFrom` unwraps `JemiStack.ingredient`. AppMek's EMI converter
    does the same.
  - Registered only when EMI, JEI and Mekanism are all loaded. `JemiPlugin.runtime` is null until the JEI runtime
    exists; the converter returns `EmiStack.EMPTY` then.
  - EMI without JEI: no chemical slots, no lookup, no encoding. Mek chemicals don't exist in EMI at all then.

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
