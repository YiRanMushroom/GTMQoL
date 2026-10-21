+++
title = "How the 7.x (pre-v8) RecipeDB grouped search worked and what was not ported"
keys = [ "catalyst handler", "expanded pattern buffer", "gtmutils", "islothint", "recipedbstatic", "reference", "targetlookup" ]
updated = "2026-10-21 01:13:03"
+++
7.x code is in git history (the `reference/` archive, removed in commit 0f6b123: `mixin/RecipeDBMixin.java`, `mixin_helper/RecipeDBStatic.java`, `api/ISlotHint.java`). 7.x had no handler groups, so the mod added its own:
- `api/ISlotHint`: each handler got a *matching group* (default itself) and a *catalyst* flag (`NotifiableRecipeHandlerTraitMixin` @Unique field; `InternalSlotRecipeHandlerMixin` made a buffer slot's item+fluid handlers return their `InternalSlot` as group; `DualHatchPartMachineMixin` put a dual hatch's inventory and tank in one group).
- Catalysts (combine with every group of the same machine): `NotifiableItemStackCatalystHandler` / `NotifiableCatalystFluidTank` subclasses, used for the bus circuit slot (`ItemBusPartMachineMixin`) and the buffer's shared inventory/tank (also GTMUtils' expanded buffer and 7.x's `AbstractMEPatternBufferPartMachine`).
- `RecipeDBMixin` `@Overwrite fromHolder` built `targetLookup[i]` (every non-distinct physical entry; for a catalyst every distinct entry of the machine; otherwise own group + the machine's catalysts; non-item/fluid entries always allowed), passed via `ThreadLocal`s in `RecipeDBStatic`. `RecipeIteratorMixin` `@Overwrite next()` used it, with an AT + reflection for `SearchFrame`/`Branch`/`nodesForIngredient`.

v8 mapping: the kind comes from the handler list (recipedb.grouped-list): bypass-distinct lists and capabilities that bypass distinct -> UNIVERSAL (other non-item/fluid caps stay in their list's group), "normal pool" -> POOLED, "group + catalysts" -> same RHL + slot/CATALYST. The ThreadLocal became the `<init>` capture of a `GroupedIngredientList`; AT/reflection became accessor mixins.

Not ported: catalyst handlers for GTMUtils' expanded pattern buffer Only gtceu's `MEPatternBufferPartMachine` and our `AbstractMEPatternBufferPartMachine` are split per slot (recipedb.pattern-buffer-split); another mod's buffer is DISTINCT as a whole if it uses a plain `RecipeHandlerList`, else (its own RHL subclass) UNIVERSAL, not restricted at all.