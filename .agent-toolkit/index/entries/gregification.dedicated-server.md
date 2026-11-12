+++
title = "GregificationClient: remote clients convert synced foreign recipes themselves to fill recipe viewer categories"
keys = [ "category map", "emi empty categories", "gregification core", "recipesupdatedevent" ]
updated = "2026-11-12 07:49:30"
checked = "2026-11-12"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/client/GregificationClient.java"
snippet = "private static void onRecipesUpdated(RecipesUpdatedEvent event) {"
+++
Only on remote clients (an integrated server already fills the same recipe types). Since 2026-11-12 the client also runs GregifiedRecipeType.prepare per proxied type before converting, as the server's RecipeManagerHandlerMixin does; before, MarkCoveredRecipes never ran on a dedicated server's client, so EMI showed no ghost marks there. On 1.21 tags come in the configuration phase, before recipes, so LoadingTags reads the right ones; 1.20 sends recipes before tags (see that branch's entry).