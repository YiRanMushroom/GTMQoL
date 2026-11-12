+++
title = "GregificationClient: remote clients convert synced foreign recipes themselves to fill recipe viewer categories"
keys = [ "category map", "emi", "empty categories", "gregification core", "jei", "recipesupdatedevent" ]
updated = "2026-11-12 07:57:03"
checked = "2026-11-12"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/client/GregificationClient.java"
snippet = "private static void onRecipesUpdated(RecipesUpdatedEvent event) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/client/GregificationClient.java"
snippet = "private static void onTagsUpdated(TagsUpdatedEvent event) {"
+++
Skipped in singleplayer (integrated server already filled the same types). Redone on CLIENT_PACKET_RECEIVED tags since joining sends recipes before tags (fan catalysts read tags). Registered from GTMQoLClient.  Also provides executor()/level() for common code (PB onLevelThread, see gregification.pb-level-thread). Since 2026-11-12 it runs GregifiedRecipeType.prepare per proxied type before converting, as the server does; before, MarkCoveredRecipes never ran on a dedicated server's client, so EMI showed no ghost marks there.