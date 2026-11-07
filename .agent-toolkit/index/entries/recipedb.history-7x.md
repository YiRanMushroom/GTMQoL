+++
title = "How the 7.x (pre-v8) RecipeDB grouped search worked and what was not ported"
keys = [ "catalyst handler", "expanded pattern buffer", "gtmutils", "islothint", "recipedbstatic", "reference", "targetlookup" ]
updated = "2026-11-07 10:17:10"
checked = "2026-11-07"

[[items]]
name = "v8-from-holder"
value = "v8 replacement of 7.x's RecipeDBMixin targetLookup: kinds per handler list (UNIVERSAL/DISTINCT/POOLED)."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/recipedb/GroupedIngredientList.java"
snippet = "public static GroupedIngredientList fromHolder(IRecipeCapabilityHolder holder) {"

[[items]]
name = "v8-init-capture"
value = "The 7.x ThreadLocal became this <init> capture of the GroupedIngredientList."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/recipedb/RecipeIteratorMixin.java"
snippet = "if (ingredients instanceof GroupedIngredientList grouped) {"

[[items]]
name = "v8-accessor"
value = "7.x's AT + reflection for SearchFrame became accessor mixins."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/recipedb/SearchFrameAccessor.java"
snippet = "public interface SearchFrameAccessor {"

[[items]]
name = "v8-buffer-split"
value = "7.x's InternalSlot matching group / catalyst handlers became the per-slot split with CATALYST shared inputs."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/recipedb/PatternBufferIngredients.java"
snippet = "static boolean addSplit(GroupedIngredientList list, RecipeHandlerList handlerList, int owner) {"
+++
7.x code is in git history (the `reference/` archive, removed in commit 0f6b123: `mixin/RecipeDBMixin.java`, `mixin_helper/RecipeDBStatic.java`, `api/ISlotHint.java`). 7.x had no handler groups, so the mod added its own:
- `api/ISlotHint`: each handler got a *matching group* (default itself) and a *catalyst* flag (`NotifiableRecipeHandlerTraitMixin` @Unique field; `InternalSlotRecipeHandlerMixin` made a buffer slot's item+fluid handlers return their `InternalSlot` as group; `DualHatchPartMachineMixin` put a dual hatch's inventory and tank in one group).
- Catalysts (combine with every group of the same machine): `NotifiableItemStackCatalystHandler` / `NotifiableCatalystFluidTank` subclasses, used for the bus circuit slot (`ItemBusPartMachineMixin`) and the buffer's shared inventory/tank (also GTMUtils' expanded buffer and 7.x's `AbstractMEPatternBufferPartMachine`).
- `RecipeDBMixin` `@Overwrite fromHolder` built `targetLookup[i]` (every non-distinct physical entry; for a catalyst every distinct entry of the machine; otherwise own group + the machine's catalysts; non-item/fluid entries always allowed), passed via `ThreadLocal`s in `RecipeDBStatic`. `RecipeIteratorMixin` `@Overwrite next()` used it, with an AT + reflection for `SearchFrame`/`Branch`/`nodesForIngredient`.

v8 mapping: the kind comes from the handler list (recipedb.grouped-list): bypass-distinct lists and capabilities that bypass distinct -> UNIVERSAL (other non-item/fluid caps stay in their list's group), "normal pool" -> POOLED, "group + catalysts" -> same RHL + slot/CATALYST. The ThreadLocal became the `<init>` capture of a `GroupedIngredientList`; AT/reflection became accessor mixins.

Not ported: catalyst handlers for GTMUtils' expanded pattern buffer. Only gtceu's `MEPatternBufferPartMachine` and our `AbstractMEPatternBufferPartMachine` are split per slot (recipedb.pattern-buffer-split); another mod's buffer is DISTINCT as a whole if it uses a plain `RecipeHandlerList`, else (its own RHL subclass) UNIVERSAL, not restricted at all.