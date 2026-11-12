+++
title = "LoadingTags / GTMaterialTags: reading tags while recipes load, before new tags are bound"
keys = [ "addreloadlistenerevent", "cannot retrieve values from custom ingredient", "getconditioncontext", "getitems cache", "gregification core", "incomplete recipe", "isincomplete", "onchange", "tags during reload", "tagsupdatedevent" ]
updated = "2026-11-12 07:35:39"
checked = "2026-11-12"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GTMaterialTags.java"
snippet = "public static synchronized List<TagKey<Item>> of(Item item) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/LoadingTags.java"
snippet = "public static @Nullable Set<Item> items(Ingredient ingredient) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/LoadingTags.java"
snippet = "public static @Nullable Set<Fluid> fluids(FluidIngredient ingredient) {"
+++
User approved LoadingTags as a shared helper. Users: GTMaterialTags (MI item widening, see gregification.mi) and the Create fan catalysts (gregification.create-fan). GTMaterialTags indexes item -> GT material tags (incl. 1x wire tags and MaterialAliasTags spellings) once per reload.

Rule (2026-11-12): conversion code never asks another mod's ingredient what it takes (Ingredient getItems / test / hasNoItems / getStackingIds, FluidIngredient getStacks / hasNoFluids, Mekanism's isIncomplete): those cache what they find for good, and conversion runs inside RecipeManager.apply (GT's RecipeManagerLateMixin), before the reload's tags are bound (first world load: no tags at all). The ingredient is the same object the other mod's recipe holds, so its own machines then took nothing from a tag until a /reload, and Mekanism's IncompleteRecipeScanner found 363 "incomplete" recipes at server start and crashed on a painting recipe's DifferenceIngredient (its logMissingTags calls getValues on a custom ingredient). Fixed by dropping the isIncomplete checks (Mekanism, both branches), LoadingTags.items / fluids (values + these tags; null for custom ingredients other than compound) for MarkCoveredRecipes inputs (Create saw, MI assembler) and MI's empty-input checks and fluid widening, the Advanced Chip Engraving product read from the output's values, and GT's lookup keys of the chemical capability (GenericStackLikeType.lookupKeys; ChemicalStackLike works a ChemicalIngredient out by its six sealed kinds and LoadingTags instead of getRepresentations / getChemicals, which cache). After the item fix the log still had 64 incomplete Mekanism recipes, all with tagged chemicals: that was the lookup. Item values alone need no tags, so getItems on items-only ingredients is safe (PB combBlock relies on that).