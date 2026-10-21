+++
title = "BlockProviderRecipeCapability (gtmqol:block): a world block as a non-consumed recipe lookup input"
keys = [ "block capability", "blockmapingredient", "recipe search filter", "recipedb", "registermapingredient" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/blockprovider/BlockProviderRecipeCapability.java"
snippet = "public final class BlockProviderRecipeCapability extends RecipeCapability<Block> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/blockprovider/BlockProviderRecipeCapability.java"
snippet = "MapIngredientTypeManager.registerMapIngredient(Block.class, CAP::getDefaultMapIngredient);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/blockprovider/BlockProviderTrait.java"
snippet = "public class BlockProviderTrait extends NotifiableRecipeHandlerTrait<Block> {"
+++
Why (2026-10-18, replaced CenterBlockCondition + a notConsumable shard): RecipeDB.addRecursive keeps one recipe per set of lookup inputs and drops the rest, and recipes with no lookup input are never found. As a search filter the block is its own lookup level. BlockProviderTrait is a controller trait; formStructure adds it as a handler by itself. Both branches.