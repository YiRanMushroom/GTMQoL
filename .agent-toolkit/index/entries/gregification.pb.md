+++
title = "Productive Bees source: 5 tiered single blocks (centrifuge converted; incubator/extractor/beehive/breeder custom logic)"
keys = [ "beecage", "comb", "custom recipe logic", "pb_centrifuge", "productive bees", "productivelib" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "gradle.properties"
snippet = "productivebees_version=mywVOObd"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/ProductiveBeesGregification.java"
snippet = "public static List<ForeignMachineType> types() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/ProductiveBeesGregification.java"
snippet = "private static void convertCentrifuge(Recipe<?> foreign, ForeignRecipeConverter.Output output) {"
+++
mywVOObd = PB 1.20.1-12.6.0; everything is NBT here. Centrifuge keeps PB's ingredients (no getItems() on tags at load), so tag outputs give GT's first item. Comb blocks only for item/NBT comb ingredients. PB 1.20 has no stability upgrade, so no chance bonus. ProductiveLib not declared at all (build.gradle: nothing used exposes it). Config gregification.productiveBees (its comment omits the breeder).