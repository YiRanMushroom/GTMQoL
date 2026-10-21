+++
title = "EarlyConfig: config/gtmqol-early.properties, options read before mods load that decide which mixins apply"
keys = [ "ae2.stickycard", "ae2.universalcircuitencoding", "ae2mixinplugin", "overclocking.overhaul", "recipedb.groupedsearch", "recipedbmixinplugin" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/EarlyConfig.java"
snippet = "public final class EarlyConfig {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/GTMQoLMixinPlugin.java"
snippet = "return EarlyConfig.OVERCLOCKING_OVERHAUL || !OVERCLOCKING_MIXINS.contains(mixinClassName);"
+++
Readers: RecipeDBMixinPlugin (whole recipedb config), GTMQoLMixinPlugin (overclocking.overhaul), AE2MixinPlugin (ae2.*); AE2_STICKY_CARD also gates the item and recipes in GTMQoL/GTMQoLAddon. GTMQoLMixinPlugin skips OverclockingLogicMixin, GTRecipeViewerWidgetMixin, GTRecipeModifiersMixin by class name, so anything else added to them is lost with the overhaul off. It also applies mixins under mekanism. only when Mekanism is in the LoadingModList. Missing keys are written back with defaults.