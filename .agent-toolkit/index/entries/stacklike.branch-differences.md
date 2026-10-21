+++
title = "Stack-like / chemicals on 1.20.1 vs 1.21: what differs (Mek 10.4 kinds, JEI/JEMI, slim gtceu in AE mixins)"
keys = [ "mekanism 10.4", "port", "stack-like core" ]
updated = "2026-10-19 20:12:49"
checked = "2026-10-21"
+++
Same design as the 1.21.1 branch. Differences (details in the linked-code entries):
- Registration: `register` only creates the cap; `registerAll` on gtceu's RecipeCapability RegisterEvent (stacklike.recipe-capability). No `registerMapIngredient`.
- Network: `writeIngredient`/`readIngredient` on `FriendlyByteBuf` instead of a StreamCodec (stacklike.type).
- Mek 10.4: four chemical kinds in one cap, JSON/NBT-based codecs, `withAmount` rebuild via creator + `TaggedChemicalStackIngredientAccessor` (stacklike.chemical). Applied Mekanistics 1.4.3.
- Viewers: JEI is the dev viewer; chemicals are real JEI ingredients (Mek registers them), lookup via `StackLikeJei` (stacklike.jei, 1.20.1 only). EMI only through JEMI (stacklike.emi-converter).
- AE: gtceu is `:slim` with no AT, so pattern buffer mixins use `@Coerce Object` + `MEPatternBufferWorkerSlotAccessor` and `@Shadow this$0` (stacklike.pattern-buffer). Bridge registered in `onRegisterMachines`.
- Jade: plain `NbtOps.INSTANCE`.