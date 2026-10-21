+++
title = "BlockProviderRecipeCapability (gtmqol:block): a world block as a non-consumed recipe lookup input"
keys = [ "block capability", "blockmapingredient", "recipe search filter", "recipedb", "registermapingredient" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/blockprovider/BlockProviderRecipeCapability.java"
snippet = "public final class BlockProviderRecipeCapability extends RecipeCapability<Block> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/blockprovider/BlockProviderRecipeCapability.java"
snippet = "MapIngredientTypeManager.registerMapIngredient(Block.class, CAP::getDefaultMapIngredient);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/blockprovider/BlockProviderTrait.java"
snippet = "public class BlockProviderTrait extends NotifiableRecipeHandlerTrait<Block> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "BlockProviderRecipeCapability.register(event);"
+++
Why (2026-10-18, replaced CenterBlockCondition + a notConsumable shard): RecipeDB.addRecursive keeps one recipe per set of lookup inputs, and recipes with no lookup input are never found; as a search filter the block is its own lookup level. 1.20.1: registered from the capability RegisterEvent; init() always runs (KubeJS may use it). getDefaultMapIngredient returns List.of, which ghost.lookup found crashes gtceu's pooling once two recipes share a node.