+++
title = "OC overhaul companions: recipe viewer OC preview replacement, multi smelter overclock moved after parallel"
keys = [ "applyoverclock", "multismelterparallel", "overclocking" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeViewerWidgetMixin.java"
snippet = "private OverclockingLogic gtmqol$replaceOverclock(OverclockingLogic logic) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeModifiersMixin.java"
snippet = "private static ModifierFunction gtmqol$overclockLast(ModifierFunction original,"
+++
Both only make sense with oc.overhaul and are switched with it (oc.toggle).