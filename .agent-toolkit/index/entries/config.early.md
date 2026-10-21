+++
title = "EarlyConfig: config/gtmqol-early.properties, options read before mods load that decide which mixins apply"
keys = [ "ae2.stickycard", "ae2.universalcircuitencoding", "ae2mixinplugin", "overclocking.overhaul", "recipedb.groupedsearch" ]
updated = "2026-10-21 08:13:49"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/EarlyConfig.java"
snippet = "public final class EarlyConfig {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/GTMQoLMixinPlugin.java"
snippet = "return EarlyConfig.OVERCLOCKING_OVERHAUL || !OVERCLOCKING_MIXINS.contains(mixinClassName);"
+++
Readers: RecipeDBMixinPlugin (recipeDB.groupedSearch, whole config), GTMQoLMixinPlugin (overclocking.overhaul), AE2MixinPlugin (ae2.*); AE2_STICKY_CARD also at runtime (GTMQoL: item + upgrades, GTMQoLAddon: recipes). GTMQoLMixinPlugin skips OverclockingLogicMixin, GTRecipeViewerWidgetMixin, GTRecipeModifiersMixin by class name, so anything else put in those classes is lost with the overhaul off; that is why the potion tooltip on the same widget is a separate mixin.