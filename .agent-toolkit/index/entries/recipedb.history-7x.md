+++
title = "How the 7.x (pre-v8) RecipeDB grouped search worked and what was not ported"
keys = [ "catalyst handler", "expanded pattern buffer", "gtmutils", "islothint", "recipedbstatic", "reference", "targetlookup" ]
updated = "2026-10-19 20:12:37"
+++
7.x code is in git history (the `reference/` archive, removed 2026-10-20: `mixin/RecipeDBMixin.java`, `mixin_helper/RecipeDBStatic.java`, `api/ISlotHint.java`). 7.x had no handler groups, so the mod added its own:
- `api/ISlotHint`: each handler got a *matching group* (default itself) and a *catalyst* flag (`NotifiableRecipeHandlerTraitMixin` @Unique field; `InternalSlotRecipeHandlerMixin` made a buffer slot's item+fluid handlers return their `InternalSlot` as group; `DualHatchPartMachineMixin` put a dual hatch's inventory and tank in one group).
- Catalysts (combine with every group of the same machine): `NotifiableItemStackCatalystHandler` / `NotifiableCatalystFluidTank` subclasses, used for the bus circuit slot (`ItemBusPartMachineMixin`) and the buffer's shared inventory/tank (also GTMUtils' expanded buffer and 7.x's `AbstractMEPatternBufferPartMachine`).
- `RecipeDBMixin` `@Overwrite fromHolder` built `targetLookup[i]` (every non-distinct physical entry; for a catalyst every distinct entry of the machine; otherwise own group + the machine's catalysts; non-item/fluid entries always allowed), passed via `ThreadLocal`s in `RecipeDBStatic`. `RecipeIteratorMixin` `@Overwrite next()` used it, with an AT + reflection for `SearchFrame`/`Branch`/`nodesForIngredient`.

v8 mapping: "non-physical" -> UNIVERSAL, "normal pool" -> POOLED, "group + catalysts" -> same RHL + slot/CATALYST (recipedb.grouped-list). The ThreadLocal became the `<init>` capture of a `GroupedIngredientList`; AT/reflection became accessor mixins.

Not ported: catalyst handlers for GTMUtils' expanded pattern buffer (an addon buffer RHL bundling patterns gets one WHOLE slot / is UNIVERSAL, see recipedb.pattern-buffer-split). The v8 `AbstractMEPatternBufferPartMachine` (our own buffers) IS split.