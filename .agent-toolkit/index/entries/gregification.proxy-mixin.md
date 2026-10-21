+++
title = "RecipeManagerHandlerMixin: takes over gtceu's proxy-recipe addition for gregified types (n recipes per foreign one)"
keys = [ "addproxyrecipestolookup", "addstaging", "gregification core", "kubejs", "recipemanagermixin" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/RecipeManagerHandlerMixin.java"
snippet = "@Inject(method = \"addProxyRecipesToLookup\", at = @At(\"HEAD\"), cancellable = true)"
+++
The user chose to own this copy of gtceu's steps; if gtceu changes RecipeManagerHandler.addProxyRecipesToLookup, ours won't follow; compare against .gtceu-src/com/gregtechceu/gtceu/api/recipe/lookup/RecipeManagerHandler.java on gtceu upgrades. recipesByID is only the proxied type's recipes from gtceu's RecipeManagerMixin but every recipe from its KubeJS plugin, hence the type filter. In gtmqol.mixins.json.