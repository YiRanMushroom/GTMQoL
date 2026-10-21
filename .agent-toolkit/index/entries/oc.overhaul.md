+++
title = "Overclocking overhaul: OverclockingLogic @Overwrites (4x/4x and 2x/4x subtick, equivalent OC, coil OC 4x/8x)"
keys = [ "getmodifier", "heatingcoiloc", "parallel", "perfect overclock", "subtickparalleloc", "ulv penalty" ]
updated = "2026-10-21 08:27:56"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/overclock/Overclocking.java"
snippet = "public static OverclockingLogic replace(OverclockingLogic logic) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/OverclockingLogicMixin.java"
snippet = "default @NotNull ModifierFunction getModifier(MetaMachine machine, GTRecipe recipe, long maxVoltage,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/OverclockingLogicMixin.java"
snippet = "static OverclockingLogic.OCResult subTickParallelOC(OverclockingLogic.OCParams params, long maxVoltage,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/OverclockingLogicMixin.java"
snippet = "static OverclockingLogic.OCResult heatingCoilOC(OverclockingLogic.OCParams params, long maxVoltage,"
+++
Not patching the constants: the 4 logic constants are static final (factors inlined); the old OverclockingPatcher swapped them with Unsafe (dropped). @Overwrite only (interface mixin). Overclocking.replace maps all four logics, subtick or not, to the subtick replacements (4x/4x, 2x/4x). getModifier computes parallels for them unless shouldParallel=false (then 1); no ULV penalty. subTickParallelOC applies to every subtick logic. heatingCoilOC ignores coil temperature. Switch: oc.toggle.