+++
title = "Productive Bees source: 5 tiered single blocks (centrifuge converted; incubator/extractor/beehive/breeder custom logic)"
keys = [ "beecage", "comb", "custom recipe logic", "pb_centrifuge", "productive bees", "productivelib" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/ProductiveBeesGregification.java"
snippet = "public static List<ForeignMachineType> types() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/ProductiveBeesGregification.java"
snippet = "private static void convertCentrifuge(RecipeHolder<?> holder, ForeignRecipeConverter.Output output) {"

[[links]]
path = "gradle.properties"
snippet = "productivebees_version=hpPcu64f"
+++
Centrifuge outputs keep PB's ingredients (no getItems() on tags at load: vanilla caches it and would poison PB's own ingredient), so tag outputs give GT's first item, not PB's mod preference. Custom logic also builds EMI representative recipes. Cage checks use BeeCage.isFilled/instanceof, so sturdy cages work (EMI shows the plain cage). ProductiveLib: compileOnly libs/ jar. Config gregification.productiveBees.