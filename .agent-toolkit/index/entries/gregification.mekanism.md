+++
title = "Mekanism 10.4 source: 23 ANY_PARTS multiblocks with chemical slots; durations from Mek tile constants"
keys = [ "base_ticks_required", "electric_ticks", "electrolytic separator", "mek_", "mekanism gregification", "precision sawmill", "rotary condensentrator" ]
updated = "2026-10-21 08:25:36"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mekanism/MekanismGregification.java"
snippet = "public static List<ForeignMachineType> types() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mekanism/MekanismGregification.java"
snippet = "private static void convertRotary(Recipe<?> foreign, ForeignRecipeConverter.Output output) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mekanism/MekanismGregification.java"
snippet = "private static final int ELECTRIC_TICKS = 200;"
+++
Mek 10.4 passes most tile tick counts as literals: ELECTRIC_TICKS (200), OXIDIZER_TICKS (100); advanced machines and dissolution use BASE_TICKS_REQUIRED, the PRC the recipe's own duration. Per-tick machines (infuser, pigment mixer, centrifuge, SNA, washer, rotary, TEP) run 1 t per operation; the separator its energy multiplier in ticks. Throughput from batch/parallels (user's call). Not gregified: smelting, fission, fusion, SPS, pumps, nucleosynthesizer, conversions. Chemicals: stacklike.