# Gregification

1.21.1 only (`gregification/`). Other mods' machine recipes run on GT machines, converted on every recipe reload.
Nothing is written to data packs or static resources. The user designed it as an abstraction layer: one source
per mod returns `ForeignMachineType`s, and everything else is shared.

## Flow

```text
GTMQoL constructor
  Gregification.init(modBus)             CatalystShapelessRecipe serializer; listeners below
  Gregification.addSource(X::types)      per mod, behind its config + ModList check
first RegisterEvent (HIGHEST)            declareAll(): every source's types (skipped in datagen)
  declare(type)                          GT recipe type + runtime lang, then machines
    single block → declareSingleBlocks    GregifiedTieredMachine per ELECTRIC_TIERS tier
    multiblock   → declareMultiblock      one controller shaped by MultiblockShape
  CRAFTED += (machine, catalyst, counterpart)
gtceu:machine RegisterEvent              ModularMachines adds modular_<name> for the single blocks
FMLCommonSetupEvent                      GregifiedRecipeType.resolveProxy(): foreign type → getProxyRecipes()
every recipe reload (server)             gtceu's RecipeManagerLateMixin → addProxyRecipesToLookup, which our mixin
                                         takes over for gregified types: toGTRecipes(holder) per foreign recipe
RecipesUpdatedEvent (remote client)      GregificationClient converts the synced foreign recipes itself
GTMQoLAddon.addRecipes                   Gregification.addRecipes: crafting recipes for CRAFTED
```

Why the first `RegisterEvent`: every mod is constructed by then, so MI's KubeJS-added recipe types exist. It also
comes before any of our registrate's registries, and before `ModularMachines` declares its multiblocks. Gtceu
reorders registries (its own and recipe types first), and we'd rather not depend on that order. The flag
`declared` makes it run once.

## Pieces

| Class | Role |
|---|---|
| `ForeignMachineType` (record) | One foreign recipe type. Fields: `name` (path of the GT type and machines, e.g. `mi_macerator`, `mek_chemical_oxidizer`), `englishName`, `proxy` (supplier of the foreign `RecipeType`, only asked for at common setup), `converter`, item/fluid IO counts, `recipeType` (extra `GTRecipeTypeBuilder` setup, via `withRecipeType`), `sound`, `ui` (progress bar etc.), `catalyst` and `counterpart` (item ids), `model` (single blocks) or `shape` (multiblocks). Factories `singleBlock(...)` and `multiblock(...)`. |
| `ForeignRecipeConverter` | `void convert(RecipeHolder<?>, Output)`: one foreign recipe to any number of GT recipes. Each `output.add(builder -> ...)` fills a fresh builder (inputs, outputs, `EUt`, `duration`) and returns false to drop that one, so a converter that bails halfway leaves nothing behind. Adding none leaves the foreign recipe out. Most converters make one recipe: write them as `ForeignRecipeConverter.Single` (`boolean convert(holder, builder)`, the old form) and wrap with `ForeignRecipeConverter.single(...)`. Several recipes: Mek rotary (one per direction). |
| `GregifiedRecipeType` / `GregifiedRecipeTypeBuilder` | GT recipe type that proxies the foreign one. `toGTRecipes` calls the converter, logs and returns nothing on exceptions, and sets ids `gtmqol:/gregification/<our type path>/<ns>/<path>`, plus `/<index>` from the second recipe of one foreign recipe on. Our type path keeps ids unique when two of our types proxy the same foreign type. The leading `/` marks it synthetic, so EMI (dev mode) doesn't warn that it isn't in the recipe manager. `toGTRecipe` (GTCEu's one-recipe API) returns the first or null; nothing of ours calls it. |
| `core/mixins/RecipeManagerHandlerMixin` | HEAD inject into `RecipeManagerHandler.addProxyRecipesToLookup`, cancelled only for `GregifiedRecipeType`: the same steps as GTCEu (clear the proxy list, filter by type, add to the list and `addStaging`), but with `toGTRecipes`. GTCEu's version takes exactly one recipe per foreign recipe, which doesn't fit ours; the user chose to own this. If GTCEu changes that method, ours won't follow. Other types are untouched. |
| `GregifiedTieredMachine` | `SimpleTieredMachine` + `BatchModeTrait` (saved flag, GT's batch button on the right configurators, default off). |
| `MultiblockShape` | Multiblock structures: `GENERIC` (3x3x3 steel, recipe-type auto abilities), `ANY_PARTS` (same box, but takes every item/fluid import/export part plus 1–2 energy hatches whatever the slot counts; for capabilities that come in through parts of other abilities, e.g. chemicals), `ELECTRIC_BLAST_FURNACE` (coils; controller is a `CoilWorkableElectricMultiblockMachine`), `VACUUM_FREEZER`, `IMPLOSION_COMPRESSOR`, `DISTILLATION_TOWER`. The GT ones are copied from `GTMultiMachines`, because the pattern functions aren't reachable from the registered definitions. Each shape gives casing, casing model and overlay, `machine()` and `configure(builder)`. |
| `GregificationModifiers` | `OVERCLOCK`, `BATCH`, `COIL_TEMPERATURE`, below. |
| `CatalystShapelessRecipe` | Serializer `gtmqol:catalyst_shapeless`: a shapeless recipe whose `catalyst` stays in the grid (like a GT tool, no durability). |
| `client/GregificationClient` | Fills the recipe viewer categories on dedicated-server clients, below. |

### Recipe types

`declare` creates `gtmqol:<name>` through `GTMQoLAddon.registrate()` with a `GregifiedRecipeTypeBuilder`. It then
calls `setMaxIOSize(items in, items out, fluids in, fluids out)`, `setEUIO(IN)`, `UI(type.ui())`,
`setSound(type.sound())` and finally `type.recipeType().apply(builder)` (e.g. `setMaxSize(IO, ChemicalStackLike.CAP,
1)`). Lang is added at runtime (`RuntimeGeneration.addLanguageEntry`). Types take the sound and progress bar of
their GT counterpart; with no counterpart, no sound and a plain arrow. Slot overlays aren't copied, because the slot
layouts differ.

The foreign type is a `Supplier` and only goes into `getProxyRecipes()` at common setup. It can't be passed to the
properties: Mekanism creates its recipe types when they are registered, which is after we declare.

### Machines

Every machine is `dynamicallyGenerated(true)`: models and lang are made at runtime, and nothing is datagen'd (the
user wants this).

- Single blocks: `<tier>_<name>` for every `ELECTRIC_TIERS` tier, with gtceu's `workableTieredHullModel(model)`,
  `GENERAL_MACHINE` UI and modifiers `OVERCLOCK`, `BATCH`. `ModularMachines` then adds `modular_<name>`. It
  uses `GregificationModifiers` too, because `Gregification.allGregified(recipeTypes)` is true.
- Multiblocks: `GTMQoLAddon.multiblock(name, shape.machine())`, then `shape.configure`. Modifiers are
  `COIL_TEMPERATURE`, `OVERCLOCK`, gtceu's `GTRecipeModifiers.BATCH_MODE`, plus the shape's appearance, pattern and
  `workableCasingModel`.

### Recipe logic (`GregificationModifiers`)

The user calls this "FE overclock logic". Recipes are converted at a fixed `VA[LV]`, keeping the foreign total
energy where the source has one.

- `OVERCLOCK`: speed `s = voltage / EUt` at the same energy per recipe. The duration is `ceil(duration / s)` and
  EU/t rises by the same factor. Past 1 tick it runs `s / duration` subtick parallels (limited by
  `ParallelLogic.getParallelAmountWithoutEU`). Only rounding to whole ticks loses speed.
- `BATCH`: gtceu's batch logic, for single blocks (reads `BatchModeTrait`). Multiblocks use gtceu's own
  `BATCH_MODE` instance instead, because `MachineUIPanelBuilder` only shows the batch button when the modifier list
  contains that exact object.
- `COIL_TEMPERATURE`: recipes with `ebf_temp` need a coil machine at least that hot. The temperature is the same as
  GT's EBF (coil + 100 K per tier above MV). There's no heat discount or perfect OC. Other recipes pass.

### Crafting

`Gregification.addRecipes` (called from `GTMQoLAddon.addRecipes`). Counterpart and catalyst are item ids resolved
at recipe time; missing ones (e.g. tiers gtceu doesn't register) are skipped with a debug log.

- With a catalyst: `gtmqol:gregification/<machine path>`, shapeless counterpart + catalyst = machine, catalyst kept
  (`CatalystShapelessRecipe`). Used by MI (its guidebook).
- Catalyst `null`: same as the modular machines. A shaped `h` over the counterpart
  (`gregification/hammer_<machine path>`; the GT hammer loses durability through GT's crafting remainder), and a
  magical assembler recipe (counterpart + circuit 5, `VA[LV]`, 200 t). Used by Mek.
- Two machines may not share a counterpart: the recipes would be identical. Craft the second from the first
  (e.g. the MI unpacker from our MI packer).

### Dedicated servers

Gtceu converts proxied recipes only where recipes are loaded, i.e. on the server, and the results aren't synced
(they aren't in the recipe manager). In singleplayer the client shares the server's types. On a remote server the
viewer would show empty categories. So `GregificationClient` runs on `RecipesUpdatedEvent` (HIGHEST, before the
viewers reload). It converts the synced foreign recipes into the categories, removing what it added the last time.
It does nothing when an integrated server exists. Not verified on a dedicated server yet.

## Sources

### Modern Industrialization (`mi/MIGregification`)

Config `gregification.modernIndustrialization`, only when `modern_industrialization` is loaded. Dependency:
`compileOnly` + `localRuntime` Modrinth `modern-industrialization` (`HOR1tVas` = 2.5.10).

- Iterates `MIMachineRecipeTypes.getRecipeTypes()`, not the registry, because MI's types may not be registered
  yet. Names are `mi_<path>`, "MI <Name>". Unknown types (e.g. KubeJS) are logged and skipped.
- 14 single blocks with MI's slot counts (`SINGLE_BLOCKS`); counterpart is the same-tier GT machine. The unpacker
  is crafted from our MI packer, since GT's packer is taken.
- 10 multiblocks with 6 slots per kind MI allows (`MULTIBLOCKS`). Counterpart is the GT controller, or MI's own
  machine when GT has none. Catalyst is MI's guidebook.
- Converter: `EUt(VA[LV])`, duration `ceil(MI total EU / VA[LV])` (1 MI EU = 1 GT EU). Recipes with process
  conditions are skipped.
  - Probabilities: 1 always; input 0 becomes chance 0 (not consumed); anything else is a chance.
  - Fluids are matched to GT by name. The GT material is the fluid id path, after `MaterialAliasTags.ALIASES`. A
    single-fluid input becomes compound(original, `#c:<material>`), and an output becomes the GT fluid. It's only a
    name guess.
- Blast furnace: MI picks the coil tier by EU/t (`ElectricBlastFurnaceBlockEntity.tiers`, `maxBaseEu`). The recipe
  gets the `blastFurnaceTemp` of the GT coil with the same index (cupronickel 1800 K, kanthal 2700 K, then
  nichrome... for KubeJS tiers). The recipe UI shows GT's coil line (`GTRecipeUIModifiers.TEMP_COIL_INFO`).

### Mekanism (`mekanism/MekanismGregification`)

Config `gregification.mekanism`, only when `mekanism` is loaded. It uses the chemical capability
(`STACK_LIKE_CAPABILITIES.md`).

- Every type is `mek_<mek machine>`, a multiblock with `MultiblockShape.ANY_PARTS`. The universal ME parts and the
  pattern buffer carry item/fluid abilities, so chemical-only recipes still find a part.
- `type(...)` in `MekanismGregification` is the one factory: slot counts incl. chemical in/out
  (`setMaxSize(IO, ChemicalStackLike.CAP, n)`), sound, progress bar, counterpart. Catalyst is always null, so every
  machine is the Mek machine hit with a GT hammer or run through the magical assembler.
- All at `VA[LV]`. The output is the first of `getOutputDefinition()` (one per possible input; Mek's own are all
  equal). Incomplete recipes (`isIncomplete()`) are skipped.
- Machines with progress: duration = the tile entity's `BASE_TICKS_REQUIRED` (`mekanism/common/tile/...`; 200 for
  most, 100 for oxidizer, pigment extractor, dissolution). A per-tick chemical input (`perTickUsage()`) is multiplied
  by the ticks. The PRC uses the recipe's `getDuration()`.
- Machines without progress, one operation per tick (chemical infuser, pigment mixer, centrifuge, activator, washer,
  separator, rotary, evaporation): amounts as in Mek, duration 1 t, i.e. one operation per tick like the Mek
  machine without upgrades. Batch mode and parallels give the throughput (the user's call; not scaled up). The
  separator's duration is `getEnergyMultiplier()` ticks (energy per operation, as time at a fixed voltage).
- Sawmill: secondary output becomes a GT chanced output (`getSecondaryChance()` × 10000), or a plain output at 100%.
- Rotary: one machine, `mek_rotary_condensentrator`, and a GT recipe per direction the Mek recipe has (chemical →
  fluid, fluid → chemical), so it condenses or decondenses by what goes in.
- Not gregified: Mek smelting (the energized smelter runs furnace recipes; GT's electric furnace covers that),
  fission, fusion, SPS, pumps, antimatter (nucleosynthesizer), energy and chemical conversion (item → energy/chemical
  in slots).

## Adding

A type to an existing source: add an entry to its table (MI) or `types()` list (Mek). The fields are slot counts,
sound/UI, counterpart and shape, plus a converter if the recipe class differs.

A source for a new mod:

1. A class `gregification/<mod>/<Mod>Gregification` with `static List<ForeignMachineType> types()`. Touch the mod's
   classes only there.
2. Converters for each foreign recipe class: inputs, outputs, `EUt`, `duration`. Usually a `Single` wrapped with
   `ForeignRecipeConverter.single`; a full `ForeignRecipeConverter` when one foreign recipe becomes several. Return
   false for anything a GT machine can't do (conditions, missing ingredients).
3. A config toggle under `gregification.*`, and in the `GTMQoL` constructor
   `if (config.gregification.<mod> && ModList.get().isLoaded("<mod>")) Gregification.addSource(<Mod>Gregification::types);`.
4. The dependency is `compileOnly` + `localRuntime`, not in `neoforge.mods.toml`.
5. If its recipes use something that isn't an item or fluid, it needs a recipe capability first (stack-like).
6. A new structure goes in `MultiblockShape`. Copy GT's pattern from `GTMultiMachines`, then swap in
   `controller(blocks(definition.getBlock()))`.

## Where to look

- Gtceu 1.21 sources (`.gtceu-src-1.21/com/gregtechceu/gtceu/`):
  - `core/mixins/RecipeManagerLateMixin.java` and `api/recipe/lookup/RecipeManagerHandler.java` (proxy conversion);
  - `api/recipe/GTRecipeType.java` (`getProxyRecipes`, `toGTRecipe`), `api/recipe/lookup/RecipeManagerHandler.java` (what our mixin replaces);
  - `common/data/machines/GTMultiMachines.java` (patterns);
  - `common/data/GTRecipeModifiers.java`;
  - `api/machine/mui/MachineUIPanelBuilder.java` (batch button).
- MI: the Modrinth jar in the Gradle cache, `aztech/modern_industrialization/machines/{init/MIMachineRecipeTypes,recipe/MachineRecipe}`.
- Mekanism sources jar (see `STACK_LIKE_CAPABILITIES.md`):
  - `mekanism/api/recipes/` (recipe classes);
  - `MekanismRecipeTypes`;
  - `mekanism/common/tile/machine/TileEntity*` (`BASE_TICKS_REQUIRED`, per-tick usage).
