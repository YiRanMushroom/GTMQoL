+++
title = "RecipeManagerHandlerMixin: takes over gtceu's proxy-recipe addition for gregified types (n recipes per foreign one)"
keys = [ "addproxyrecipestolookup", "addstaging", "gregification core", "recipemanagerlatemixin" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/RecipeManagerHandlerMixin.java"
snippet = "@Inject(method = \"addProxyRecipesToLookup\", at = @At(\"HEAD\"), cancellable = true)"
+++
The user chose to own this copy of gtceu's steps; if gtceu changes RecipeManagerHandler.addProxyRecipesToLookup (called from its RecipeManagerLateMixin each reload), ours won't follow. Compare against .gtceu-src-1.21/com/gregtechceu/gtceu/api/recipe/lookup/RecipeManagerHandler.java on gtceu upgrades.