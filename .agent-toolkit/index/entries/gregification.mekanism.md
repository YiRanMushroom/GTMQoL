+++
title = "Mekanism source: 23 ANY_PARTS multiblocks with chemical slots; durations from Mek tile BASE_TICKS_REQUIRED"
keys = [ "electrolytic separator", "mek_", "mekanism gregification", "pertickusage", "precision sawmill", "rotary condensentrator" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mekanism/MekanismGregification.java"
snippet = "public static List<ForeignMachineType> types() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mekanism/MekanismGregification.java"
snippet = "private static void convertRotary(RecipeHolder<?> holder, ForeignRecipeConverter.Output output) {"
+++
Per-tick machines (infuser, mixer, centrifuge, activator, washer, separator, rotary, evaporation) run 1 t per Mek operation; throughput comes from batch/parallels, not scaled up (user's call). Not gregified: smelting (GT furnace covers it), fission, fusion, SPS, pumps, nucleosynthesizer, energy conversion. Chemicals: stack-like capability entries.