+++
title = "RecipeDB grouped-search mixins: fromHolder overwrite, frame-push filter in getNext's lambda, SearchFrame accessor"
keys = [ "lambda$getnext$0", "wrapwithcondition" ]
updated = "2026-10-19 20:12:37"

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
fromHolder walks getCapabilitiesForIO(IO.IN), not the flattened map. Another mod's injection into fromHolder is lost by the @Overwrite (accepted). Lists not from our fromHolder (RecipeDB.find(Map)) search as before. The push is filtered instead of rewriting the DFS: @Accessor matches by field descriptor, so package-private Branch can't be read via Object. `lambda$getNext$0` is javac's default name; after a GTCEu bump check `javap -p` on RecipeDB$RecipeIterator.