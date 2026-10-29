+++
title = "overclocking.overhaul early option: GTMQoLMixinPlugin skips the four OC overhaul mixins by class name"
keys = [ "gtmqol-early.properties", "overclocking_overhaul" ]
updated = "2026-10-29 06:19:16"
checked = "2026-10-29"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/GTMQoLMixinPlugin.java"
snippet = "private static final Set<String> OVERCLOCKING_MIXINS = Set.of("

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/EarlyConfig.java"
snippet = "defaults.setProperty(\"overclocking.overhaul\", \"true\");"
+++
Early option because @Overwrite can't be switched off at runtime. Restart required. The four are OverclockingLogicMixin, GTRecipeViewerWidgetMixin, GTRecipeModifiersMixin and ResearchOverclockMixin (oc.research). Anything else added to those mixin classes disappears with the overhaul too.