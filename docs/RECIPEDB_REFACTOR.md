# RecipeDB grouped search

How GTMQoL speeds up GTCEu's recipe lookup on machines with many distinct inputs: the idea, how the
7.x version did it, and how it was ported to v8.

## The problem

`RecipeDB` stores recipes as a tree: each level is one ingredient, and a leaf is a recipe. To find a recipe,
`RecipeDB.RecipeIterator` runs a depth-first search over the machine's input ingredients:

1. `fromHolder` flattens every input handler of the machine into one list of ingredient entries.
2. The root pushes a search frame for every entry.
3. Whenever an entry's ingredient matches a node that is a branch, it pushes a frame for **every** entry
   again (`for (int j = ingredients.size() - 1; j >= 0; j--) stack.push(new SearchFrame(j, b))`).

Step 3 tries every combination of everything the machine has. With a few buses that is fine. Late game,
with a multiblock fed by many distinct buses or pattern buffers (dozens of patterns, each holding a full
set of inputs), the number of paths grows combinatorially. Most of them mix ingredients from two different
buses or patterns, and the recipe runner would reject them anyway. In testing this caused severe lag.

## The idea

A recipe can only run if one handler group supplies all of its inputs: one distinct bus, or one pattern
slot plus the buffer's shared inventory and circuit, or the machine's non-distinct pool. So during the
search, after an entry from group G, only descend into entries that could be in the same group as G.

The search order is still the tree's order. Only the frames pushed at step 3 are filtered. The filter is a
relation between pairs of entries ("`to` may come right after `from`"). It is checked only between
neighbours on a path, so it is a relaxation: it never drops a recipe the runner would accept. It only
prunes paths that mix groups.

## 7.x implementation (archived in `reference/`)

7.x had no notion of groups in the recipe handlers, so the mod added its own:

- `api/ISlotHint` gave each handler a *matching group* (default: the handler itself) and a *catalyst* flag.
  - `mixin/NotifiableRecipeHandlerTraitMixin` implements it with a `@Unique` field.
  - `mixin/InternalSlotRecipeHandlerMixin` makes a pattern buffer slot's item and fluid handlers return their
    `InternalSlot` as the group.
  - `mixin/DualHatchPartMachineMixin` puts a dual hatch's inventory and tank into one group.
- Catalysts are inputs that combine with every group of the same machine:
  - `api/NotifiableItemStackCatalystHandler` and `api/NotifiableCatalystFluidTank` are handler subclasses
    with the flag set.
  - `mixin/ItemBusPartMachineMixin` uses them for the circuit slot.
  - `mixin/MEPatternBufferPartMachineInternalInventoryAbstractionMixin` uses them for the buffer's shared
    inventory and tank. The same file also held the unrelated expanded-buffer changes.
  - `integration/ae2/AbstractMEPatternBufferPartMachine` and the GTMUtils expanded-buffer mixin did the same
    for those buffers.
- `mixin/RecipeDBMixin` `@Overwrite fromHolder` sorts every entry into pools by machine, distinctness,
  group and catalyst flag. It builds `targetLookup[i]`, the entries allowed after entry `i`:
  - every non-distinct physical entry;
  - for a catalyst: every distinct entry of the same machine;
  - for any other distinct entry: its own group plus its machine's catalysts.

  "Non-physical" entries (anything that isn't an item or fluid map ingredient) were always allowed. The
  lookup was handed to the iterator through `ThreadLocal`s in `mixin_helper/RecipeDBStatic`.
- `RecipeDBMixin$RecipeIteratorMixin`:
  - `@Overwrite next()` with the same DFS, which pushed only `targetLookup[current]` plus the non-physical
    entries;
  - an access transformer and reflection (`RecipeDBStatic`) to reach the private `SearchFrame`, `Branch`
    and `nodesForIngredient`.

## What changed in v8

v8 models the groups itself, which removes most of the 7.x work:

- Every part exposes `RecipeHandlerList`s (RHL), and every RHL has a `RecipeHandlerGroup`:
  - `BUS_DISTINCT`: a distinct bus.
  - `RecipeHandlerGroupColor(c)`: dyed; `UNDYED` is -1 and means the non-distinct pool.
  - `BYPASS_DISTINCT`: an RHL is treated this way when `doesCapabilityBypassDistinct()`, e.g. energy.
- An RHL already bundles what belongs together. A bus's RHL holds its inventory *and* its circuit slot, and
  a dual hatch's RHL holds its items and fluids. So `ISlotHint`, the catalyst handler subclasses and the bus,
  dual hatch and internal-slot mixins are obsolete.
- `RecipeRunner` (`handleContents`, grouping via `RecipeHelper.addToRecipeHandlerMap`) decides what may
  combine:
  - each `BUS_DISTINCT` RHL is tried on its own, plus all bypass RHLs;
  - each colored group is tried together with the undyed RHLs, plus bypass. It never mixes two colors.
  - Undyed is tried on its own as well.
- The pattern buffer changed:
  - It is one `BUS_DISTINCT` RHL (`MEPatternBufferPartMachine$BufferRecipeHandlerList`) containing the
    circuit slot, the shared inventory and tank, and two *aggregate* handlers. Their `getContents()` is
    every worker slot's contents together.
  - There is one worker per buffer plus one per proxy. A worker holds one pattern's inputs at a time.
  - `handleRecipe` uses one worker plus the circuit and shared inventory/tank.
  - A proxy's RHL (`ProxySlotRecipeHandler$ProxyRHL`) forwards to the buffer's handlers.
- The search itself is unchanged: `fromHolder` (private) flattens `getCapabilitiesFlat()`. The DFS moved
  from `next()` into `private getNext()`, which `hasNext()`/`next()` cache. Step 3 is the lambda
  `b -> { for (j...) stack.push(new SearchFrame(j, b)); }` passed to `Either.ifRight`. `SearchFrame` is
  private and `Branch` is package-private.

## v8 implementation

Files:

- Non-mixin code in `src/main/java/com/yiran/minecraft/gtmqol/recipedb/`.
- Mixins in `mixin/recipedb/`.
- Mixin config `gtmqol.recipedb.mixins.json`.

### `GroupedIngredientList`

An `ObjectArrayList<List<AbstractMapIngredient>>`, so it can be returned from `fromHolder` as is. It
records three things for each entry:

| kind        | source                                                                | group                 | slot                          |
|-------------|-----------------------------------------------------------------------|-----------------------|-------------------------------|
| `UNIVERSAL` | RHL with `doesCapabilityBypassDistinct()`, a cap with `shouldBypassDistinct()`, or an unknown group | — | — |
| `DISTINCT`  | `BUS_DISTINCT` RHL                                                    | index of that RHL     | `WHOLE`, buffer worker, or `CATALYST` |
| `POOLED`    | `RecipeHandlerGroupColor` RHL                                         | color (-1 = undyed)   | —                             |

`canFollow(from, to)` says whether entry `to` may come right after entry `from`:

- Anything is allowed next to a `UNIVERSAL` entry.
- `DISTINCT` and `POOLED` never mix.
- Two `POOLED` entries need the same color, or one of them undyed.
- Two `DISTINCT` entries need the same RHL, and then either the same slot or one of them a `CATALYST`.

This reproduces the old rules:

- the 7.x "non-physical" entries map to `UNIVERSAL`;
- the "normal pool" maps to `POOLED`;
- "group + catalysts of the same machine" maps to same RHL plus slot/`CATALYST`.

It is stricter than 7.x in one respect: 7.x let distinct entries combine with the non-distinct pool, but
v8's runner never accepts that combination, so pruning it loses nothing.

### `PatternBufferIngredients` (only touched when AE2 is loaded)

This splits a pattern buffer's RHL, or a proxy's, the way `BufferRecipeHandlerList.handleRecipe` uses it:

- the circuit slot, the shared inventory and the shared tank become `CATALYST` entries;
- each worker's `InternalSlot` items and fluids become their own slot.

The two RHL classes are recognised by their enclosing class. The buffer is found through the
`getMachine()` of one of the RHL's traits; a proxy resolves it with `getBuffer()`.

### Mixins (`mixin/recipedb/`)

- `RecipeDBMixin`: `@Overwrite fromHolder` returns `GroupedIngredientList.fromHolder(holder)`, or null if it
  is empty, like the original. It walks `holder.getCapabilitiesForIO(IO.IN)` instead of the flattened
  map; `addHandlerList` fills both from the same RHLs.
- `RecipeIteratorMixin`:
  - `<init>` RETURN keeps the list if it is a `GroupedIngredientList`. A list from `RecipeDB.find(Map, ...)`
    or from tests isn't one, so it is searched as before. This replaces 7.x's `ThreadLocal`.
  - `@WrapOperation` on `Either.ifRight` in `getNext` records the current frame's index. The frame is
    still on top of the stack at that point.
  - `@WrapOperation` on `Deque.push` in `lambda$getNext$0` drops frames whose index can't follow it.
- Accessor interfaces, used instead of 7.x's AT and reflection:
  - `SearchFrameAccessor` (`index` of the private `RecipeDB$SearchFrame`);
  - `MEPatternBufferPartMachineAccessor` (`workers`);
  - `MEPatternBufferWorkerAccessor` (`slot` of the package-private `Worker`).

  Frames and workers are cast through `Object`. Mixin matches accessors by the field's descriptor, so a
  `Branch` field can't be read with an `Object`-typed accessor. That is why the push is filtered rather than
  the DFS rewritten.

Cost: each branch still loops over all entries and allocates their frames before the filter. That is
O(entries) per visited branch, against the combinatorial number of branches it removes.

### Config: `RecipeDBMixinPlugin`

The `plugin` of `gtmqol.recipedb.mixins.json`. Mixin configs are read before any mod is constructed, so
`GTMQoLConfig` (toma, `gtmqol.yaml`) isn't available. Like GTCEu's `GTMixinPlugin` and
`gtceu-early.properties`, the plugin reads `config/gtmqol-early.properties` with `java.util.Properties`:

```properties
recipeDB.groupedSearch=true
```

The file is created with the default (`true`) if the key is missing. When it is `false`,
`shouldApplyMixin` returns false for every mixin in the set, and GTCEu's search is untouched. Requires a
restart.

## Not ported

- 7.x catalyst handlers for GTMUtils' expanded pattern buffer, and `AbstractMEPatternBufferPartMachine`.
  If a v8 addon brings its own buffer RHL that bundles several patterns, it gets one `WHOLE` slot. That is
  correct, just not pruned inside it; add a split like `PatternBufferIngredients` for it.
- Duplicate handlers aren't deduplicated, the same as the original `fromHolder`.

## Verified (in game)

- Mixins apply, including the `lambda$getNext$0` selector (javac's default name; if a GTCEu update breaks
  it, `javap -p` on `RecipeDB$RecipeIterator` shows the real one).
- The log line `RecipeDB grouped search enabled`, and `config/gtmqol-early.properties` is created.
- Recipes still found:
  - a single-block machine;
  - a multiblock with undyed buses;
  - two buses dyed differently (a recipe split across them must not run);
  - a distinct bus with a circuit;
  - a pattern buffer with the circuit / shared inventory, and a proxy.
- Lag: a multiblock with many distinct buses / pattern buffer workers, with the option on and off.

When GTCEu is upgraded, re-check the targets above: `fromHolder`, `getNext`, the lambda, `SearchFrame.index`,
the pattern buffer's `workers` / `Worker.slot`, and the `RecipeRunner` grouping rules `canFollow` mirrors.
