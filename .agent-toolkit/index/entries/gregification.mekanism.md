+++
title = "Mekanism source: 23 ANY_PARTS multiblocks with chemical slots; durations from Mek's tiles or recipes"
keys = [ "electrolytic separator", "mek_", "mekanism gregification", "pertickusage", "precision sawmill", "rotary condensentrator" ]
updated = "2026-10-21 08:11:19"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mekanism/MekanismGregification.java"
snippet = "public static List<ForeignMachineType> types() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mekanism/MekanismGregification.java"
snippet = "private static void convertRotary(RecipeHolder<?> holder, ForeignRecipeConverter.Output output) {"
+++
Per-tick machines (infuser, pigment mixer, isotopic centrifuge, activator, washer, rotary, evaporation) run 1 t per Mek operation; the separator runs its energy multiplier in ticks; the PRC uses the recipe's own duration; others the tile's BASE_TICKS_REQUIRED. Throughput comes from batch/parallels, not scaled up (user's call). Not gregified: smelting (GT furnace covers it), fission, fusion, SPS, pumps, nucleosynthesizer, energy conversion. Chemicals: stack-like entries.