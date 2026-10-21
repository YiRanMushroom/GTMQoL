+++
title = "GregifiedRecipeType: GT recipe type proxying foreign types; synthetic recipe ids, late proxy resolution"
keys = [ "buildrawrecipe", "declarerecipetype", "getproxyrecipes", "gregification core", "resolveproxy", "togtrecipes" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GregifiedRecipeType.java"
snippet = "public List<GTRecipe> toGTRecipes(Recipe<?> foreign) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GregifiedRecipeType.java"
snippet = "public void resolveProxy() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);"
+++
Ids: gtmqol:/gregification/<our type>/<ns>/<path>[/<index>]; leading '/' = synthetic (not in the recipe manager, EMI won't warn). Built with buildRawRecipe, skipping gtceu's JSON round trip. Registered by hand in declareRecipeType (no builder class on 1.20.1; GTRecipeTypes.register only makes plain types). Lang added at runtime (RuntimeGeneration). Proxies resolved at common setup: Mek types are deferred registry objects, the Create fan reads Create's registry.