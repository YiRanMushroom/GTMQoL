+++
title = "overclocking.overhaul early option: GTMQoLMixinPlugin skips the three OC overhaul mixins by class name"
keys = [ "gtmqol-early.properties", "overclocking_overhaul" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/GTMQoLMixinPlugin.java"
snippet = "private static final Set<String> OVERCLOCKING_MIXINS = Set.of("

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/config/EarlyConfig.java"
snippet = "defaults.setProperty(\"overclocking.overhaul\", \"true\");"
+++
Early option because @Overwrite can't be switched off at runtime. Restart required. Anything else added to those three mixin classes disappears with the overhaul too.