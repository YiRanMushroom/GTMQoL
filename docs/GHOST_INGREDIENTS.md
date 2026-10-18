# Ghost ingredients (`common/ghost/`)

Both branches. A ghost ingredient is a recipe input that is never consumed and that only a "mark" satisfies, never
a real item or fluid. It tells a machine which recipe to run when the real inputs fit several (e.g. Create's saw
making planks or stairs from the same log). The user designed it as a small API: a recipe adds
`.input(GhostIngredientRecipeCapability.CAP, new ItemGhostIngredient(stack))`, and a recipe type that takes them
needs `setMaxSize(IO.IN, CAP, n)`.

## Pieces

| Class | Role |
|---|---|
| `GhostIngredient<T>` | A marked thing: a registered `Type` (string id, codecs, how to make one from what the player holds) and a value. Equal when type and value match, amount ignored. Immutable. New kinds are a subclass plus a `Type`. |
| `ItemGhostIngredient`, `FluidGhostIngredient` | The two kinds: an item with its components (left click with it), a fluid with its components (right click with a container of it). Amounts kept at 1. |
| `GhostIngredientRecipeCapability` | The capability. A recipe search filter, so recipes that differ only in the mark are told apart. `shouldBypassDistinct()` is false (see below). |
| `GhostMapIngredient` | Its recipe lookup node, its own class so it never equals a real item or fluid node. |
| `GhostIngredientHandler` | `NotifiableRecipeHandlerTrait` holding a `GhostIngredientList`; satisfies inputs it has marked, never consumes. `buildList(syncManager)` makes the list UI (`LIST_WIDTH` × `LIST_HEIGHT`). |
| `GhostIngredientList` | The marks in order, no length limit, saved through `@SaveField`; entries whose mod is gone are dropped on load. |
| `GhostIngredientListWidget` | One row per mark plus an empty row to add to. Left click marks the held item, right click what the held container holds, empty hand removes; recipe viewer drags mark what is dragged (fluids too with EMI). |
| `GhostIngredientHatchPartMachine` | The Ghost Ingredient Hatch (`GhostIngredients.GHOST_INGREDIENT_HATCH`). Ability `GHOST_INGREDIENTS`, also counts as an input bus so any multiblock takes it. Front overlay is gtceu's configurable maintenance hatch gear. |
| `GhostMarksTrait` | The same list for single blocks (they can't have the hatch): a `GhostIngredientHandler` that is also an `IAttachConfiguratorsTrait`, adding a button on the left of GT's machine UI that opens the list in a popup. Attached by `SingleblockFeature.GHOST_MARKS` (`GREGIFICATION.md`) when one of the machine's recipe types takes ghost ingredients. |
| `GhostIngredientRecipeViewer`, `GhostIngredientEmi` | Recipe viewer slots (catalysts in the input column). One capability holds items and fluids, but a viewer slot has one stack class, so the layout places empty holders and the content builder puts a slot of the right class into each. |

## Saving

`@SaveField` only saves a trait that is attached with a name (`attachPersistentTrait`) or held in a `@SaveField`
field of the machine. A plain `attachTrait` is not saved, and `markAsChanged()` only marks the block entity dirty. The
hatch keeps its handler in a `@SaveField` field; single blocks attach `"ghostMarks"` persistently.

## Distinctness and the pattern buffer

- Marks are not "bypass distinct": a ghost hatch belongs to the shared (undyed, non-distinct) group like a normal
  bus, so a distinct or dyed bus group only sees marks in its own group.
- gtceu's pattern buffer only runs a recipe from a slot that provides all of it. The marks are in a separate hatch,
  so a buffer slot alone never matched a recipe with a ghost input ("insufficient"). Our buffers have
  **Shared Hatches** (`integration/ae2/SharedHatches`, on by default; `AE2.md`): what a slot is missing may come from
  the machine's shared parts. The hatch's tooltip says to turn it on.
- The overclocked pattern buffer can also mark each pattern's main output itself (**Mark Pattern Outputs**,
  `PatternOutputMarks`, off by default; `AE2.md`).

## Marking only the recipes that need it (`gregification/MarkCoveredRecipes`)

A converter wrapper for gregified machines that choose between recipes by hand or by a filter (Create's saw, MI's
assembler). `prepare(recipes)` (called once per reload with all recipes of the foreign type, before converting)
finds the recipes whose every input takes something another recipe's inputs also take (same input, or part of it;
counts not compared). Only those get their main output as a ghost input; the others run without a mark. Inputs are
item sets per ingredient (`Function<Recipe<?>, List<Set<Object>>>`), the mark comes from a second function.

## Pitfalls hit

- `getDefaultMapIngredient` must return a mutable list: gtceu's `MapIngredientPool.applyPooling` replaces elements in
  place. `List.of` crashed world loading (`UnsupportedOperationException`) as soon as two recipes had the same mark.
- gtceu shows `<ns>.machine.<id>.tooltip` on its own; adding it again with `.tooltips` shows it twice.
