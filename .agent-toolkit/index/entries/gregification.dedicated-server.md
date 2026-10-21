+++
title = "GregificationClient: remote clients convert synced foreign recipes themselves to fill recipe viewer categories"
keys = [ "category map", "emi empty categories", "gregification core", "recipesupdatedevent" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/client/GregificationClient.java"
snippet = "private static void onRecipesUpdated(RecipesUpdatedEvent event) {"
+++
Also provides executor()/level() for common-side code (PB onLevelThread, see gregification.pb-level-thread).