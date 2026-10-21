+++
title = "RecipeDB grouped-search mixins: fromHolder overwrite, frame-push filter in getNext's lambda, SearchFrame accessor"
keys = [ "lambda$getnext$0", "wrapwithcondition" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/recipedb/RecipeDBMixin.java"
snippet = "private @Nullable List<List<AbstractMapIngredient>> fromHolder(@NotNull IRecipeCapabilityHolder holder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/recipedb/RecipeIteratorMixin.java"
snippet = "@WrapWithCondition(method = \"lambda$getNext$0\","

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/recipedb/RecipeIteratorMixin.java"
snippet = "private Either<?, ?> gtmqol$rememberFrom(Either<?, ?> result, Consumer<?> pushBranch,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/recipedb/SearchFrameAccessor.java"
snippet = "public interface SearchFrameAccessor {"
+++
fromHolder walks getCapabilitiesForIO(IO.IN) instead of the flattened map. Another mod's injection into fromHolder is lost by the @Overwrite; the user chose not to guard against it. Why filter the push instead of rewriting the DFS: mixin matches @Accessor by field descriptor, so the package-private Branch can't be read via an Object-typed accessor. `lambda$getNext$0` is javac's default name; if a GTCEu bump breaks it, `javap -p` on RecipeDB$RecipeIterator shows the real one.