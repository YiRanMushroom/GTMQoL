+++
title = "GregifiedRecipeType: GT recipe type proxying foreign types; synthetic recipe ids, late proxy resolution"
keys = [ "getproxyrecipes", "gregification core", "resolveproxy", "togtrecipes" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GregifiedRecipeType.java"
snippet = "public List<RecipeHolder<GTRecipe>> toGTRecipes(RecipeHolder<?> holder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GregifiedRecipeType.java"
snippet = "public void resolveProxy() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GregifiedRecipeTypeBuilder.java"
snippet = "protected GTRecipeType createEntry() {"
+++
Ids: gtmqol:/gregification/<our type>/<ns>/<path>[/<index>]; leading '/' = synthetic so EMI (dev) doesn't warn it's missing from the recipe manager. Type sound/progress bar copied from the GT counterpart; slot overlays not (layouts differ). Lang added at runtime (RuntimeGeneration). Proxies resolved at common setup because Mek creates its types on registration and the Create fan depends on Create's registry.