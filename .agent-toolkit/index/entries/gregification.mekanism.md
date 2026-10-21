+++
title = "Mekanism 10.4 source: 23 ANY_PARTS multiblocks with chemical slots; durations from Mek tile constants"
keys = [ "base_ticks_required", "electric_ticks", "electrolytic separator", "mek_", "mekanism gregification", "precision sawmill", "rotary condensentrator" ]
updated = "2026-10-19 20:12:37"

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
Mek 10.4 passes most tile tick counts as literals, so ELECTRIC_TICKS/OXIDIZER_TICKS repeat them; advanced machines and dissolution use BASE_TICKS_REQUIRED. Per-tick machines run 1 t per Mek operation; throughput from batch/parallels (user's call). Not gregified: smelting, fission, fusion, SPS, pumps, nucleosynthesizer, energy/infuse conversion. Item ingredients converted by hand from Mek's two ItemStackIngredient impls. Chemicals: stacklike entries.