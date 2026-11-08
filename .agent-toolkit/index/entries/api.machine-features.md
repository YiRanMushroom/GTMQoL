+++
title = "MachineFeatures: what we add to GTCEu's and other addons' machines, declared in one place, not a mixin per class"
kind = "design"
keys = [ "attach trait", "machine features", "panel configurator", "persistent trait" ]
updated = "2026-11-08 19:28:01"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/api/machine/MachineFeatures.java"
snippet = "public final class MachineFeatures {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MachineBuilderMixin.java"
snippet = "instanceFactory = MachineFeatures.wrap(instanceFactory);"
+++
User's idea (2026-11-08): one hook into machine creation, features declared as data. MachineBuilderMixin wraps every builder's instance factory at register() HEAD (the BE factory lambda reads the field when it runs), so onCreated runs after the whole constructor, on both sides, before saved data is read: the place for persistent traits. onPanel runs from MachineUIPanelBuilderMixin, also for panels without trait configurators. Used by SteamTweaks (circuit slot, tanks, bus circuit) and AutoBuildButton.