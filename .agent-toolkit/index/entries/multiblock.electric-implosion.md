+++
title = "Electric Implosion Compressor: TNT implosion recipes copied without TNT at 4x duration, via implosion onRecipeBuild"
keys = [ "_electric", "electric implosion", "electricimplosioncompressor", "onsave", "prototype builder" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/implosion/ElectricImplosion.java"
snippet = "GTRecipeTypes.IMPLOSION_RECIPES.onRecipeBuild((builder, provider) -> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/implosion/ElectricImplosion.java"
snippet = "private static void addElectricCopy(GTRecipeBuilder builder, Consumer<FinishedRecipe> provider) {"
+++
Port of the 1.19 machine, without its two mixins. Only the vanilla-TNT variant (implode_<x>_tnt) is copied, as implode_<x>_electric. 1.20's GTRecipeType has no prototype getter, so the previous onSave is read from a recipeBuilder(dummy) copy and chained; someone setting it after us without chaining drops ours. KubeJS / datapack implosion recipes get no copy (no late regeneration on 1.20.1, unlike 1.21's RecipeManagerLateMixin). Controller PCP/FSF/PCP.