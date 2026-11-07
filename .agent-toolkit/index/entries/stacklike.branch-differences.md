+++
title = "Stack-like / chemicals on 1.20.1 vs 1.21: what differs (Mek 10.4 kinds, JEI/JEMI, slim gtceu in AE mixins)"
keys = [ "mekanism 10.4", "port", "stack-like core" ]
updated = "2026-11-07 09:55:58"
checked = "2026-11-07"

[[items]]
name = "register"
value = "register only creates the cap (no registerMapIngredient)"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeRecipeCapability.java"
snippet = "public static void registerAll(GTCEuAPI.RegisterEvent<ResourceLocation, RecipeCapability<?>> event) {"

[[items]]
name = "network"
value = "FriendlyByteBuf ingredient I/O instead of a StreamCodec"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeType.java"
snippet = "void writeIngredient(FriendlyByteBuf buf, I ingredient);"

[[items]]
name = "chemical"
value = "Mek 10.4 chemicals as one stack-like type"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/mekanism/ChemicalStackLike.java"
snippet = "public final class ChemicalStackLike implements GenericStackLikeType<ChemicalStack<?>, ChemicalStackIngredient<?, ?>> {"

[[items]]
name = "worker-slots"
value = "Raw workers list + slot accessor (slim gtceu, no AT)"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferStackLikeMixin.java"
snippet = "for (Object worker : workers) slots.add(((MEPatternBufferWorkerSlotAccessor) worker).gtmqol$getWorkerSlot());"

[[items]]
name = "emi-jemi"
value = "EMI only through JEMI"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/mekanism/ChemicalEmiConverter.java"
snippet = "public final class ChemicalEmiConverter implements EmiStackConverter.Converter<ChemicalStack> {"

[[items]]
name = "jade-ops"
value = "Jade uses plain NbtOps.INSTANCE"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/jade/StackLikeRecipeOutputProvider.java"
snippet = "cap.type.ingredientCodec().encodeStart(NbtOps.INSTANCE, ingredient).result().ifPresent(list::add);"
+++
Same design as the 1.21.1 branch. Differences (details in the linked-code entries):
- Registration: `register` only creates the cap; `registerAll` on gtceu's RecipeCapability RegisterEvent (stacklike.recipe-capability). No `registerMapIngredient`.
- Network: `writeIngredient`/`readIngredient` on `FriendlyByteBuf` instead of a StreamCodec (stacklike.type).
- Mek 10.4: four chemical kinds in one cap, JSON/NBT-based codecs, `withAmount` rebuild via creator + `TaggedChemicalStackIngredientAccessor` (stacklike.chemical). Applied Mekanistics 1.4.3.
- Viewers: JEI is the dev viewer; chemicals are real JEI ingredients (Mek registers them), lookup via `StackLikeJei` (stacklike.jei, 1.20.1 only). EMI only through JEMI (stacklike.emi-converter).
- AE: gtceu is `:slim` with no AT, so `MEPatternBufferStackLikeMixin` shadows `workers` as a raw `List` and reads each worker's slot through `MEPatternBufferWorkerSlotAccessor` (1.21.1 makes them public in its AT) (stacklike.pattern-buffer). Bridge registered in `onRegisterMachines`.
- Jade: plain `NbtOps.INSTANCE`.