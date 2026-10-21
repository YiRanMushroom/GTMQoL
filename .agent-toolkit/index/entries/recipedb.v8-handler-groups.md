+++
title = "GTCEu v8 RecipeHandlerList groups and how RecipeRunner combines them (the rules grouped search mirrors)"
keys = [ "addtorecipehandlermap", "bufferrecipehandlerlist", "bus_distinct", "bypass_distinct", "distinct", "dyed", "mepatternbufferpartmachine", "proxyrhl", "recipehandlergroup", "recipehandlergroupcolor", "rhl", "undyed", "worker" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"
+++
Upstream GTCEu v8 behaviour (1.20.1 sources in `.gtceu-src/`; re-verify after GTCEu bumps).

- Every part exposes `RecipeHandlerList`s (RHL); every RHL has a `RecipeHandlerGroup` (default `RecipeHandlerGroupColor.UNDYED`):
  - `RecipeHandlerGroupDistinctness.BUS_DISTINCT`: a distinct bus.
  - `RecipeHandlerGroupColor(c)`: dyed; `UNDYED` is color -1 = the non-distinct pool.
  - `BYPASS_DISTINCT`: what an RHL is filed under when `doesCapabilityBypassDistinct()` (e.g. energy).
- An RHL bundles what belongs together: a bus's RHL holds its inventory *and* circuit slot, a dual hatch's RHL holds items and fluids. (This is why 7.x's `ISlotHint`/catalyst handlers are obsolete, see recipedb.history-7x.)
- `RecipeRunner.handleContents` (grouping via `RecipeHelper.addToRecipeHandlerMap`) decides what may combine:
  - each `BUS_DISTINCT` RHL on its own, plus all bypass RHLs;
  - each colored group together with the undyed RHLs, plus bypass; never two colors;
  - undyed on its own as well.
  - Never distinct + non-distinct (except our Shared Hatches on pattern buffers, see recipedb.grouped-list).
- Pattern buffer: one `BUS_DISTINCT` RHL (`MEPatternBufferPartMachine$BufferRecipeHandlerList`) with the circuit slot, shared inventory and tank, and two *aggregate* handlers whose `getContents()` is every worker's contents. `workers` is a `List<Worker>` (package-private `static class Worker`, `final InternalSlot slot`). One worker per buffer plus one per proxy; a worker holds one pattern's inputs at a time. `handleRecipe` uses one worker plus circuit and shared inventory/tank. A proxy's RHL (`ProxySlotRecipeHandler$ProxyRHL`, private, also BUS_DISTINCT) forwards to the buffer's handlers.
- `RecipeDB`: `fromHolder` (private) flattens `getCapabilitiesFlat().get(IO.IN)`; the DFS is in `private getNext()` (cached by `hasNext()`/`next()`); `SearchFrame` is private, `Branch` package-private.