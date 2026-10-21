+++
title = "Electric Implosion Compressor: TNT implosion recipes copied without TNT at 4x duration, via implosion onSave"
keys = [ "_electric", "electric implosion", "electricimplosioncompressor", "prototype builder", "recipemanagerlatemixin" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/implosion/ElectricImplosion.java"
snippet = "private static void hookImplosionRecipes() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/implosion/ElectricImplosion.java"
snippet = "private static void addElectricCopy(GTRecipeBuilder builder, RecipeOutput provider) {"
+++
Port of the 1.19 machine, without its two mixins. Only the vanilla-TNT variant (implode_<x>_tnt) is copied, as implode_<x>_electric. Someone setting onSave after us without chaining drops ours. On 1.20.1 KubeJS implosion recipes get no copy (no late regeneration there). Controller recipe PCP/FSF/PCP as in 1.19.