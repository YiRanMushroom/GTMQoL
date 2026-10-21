+++
title = "Overclocking overhaul: OverclockingLogic @Overwrites (4x/4x and 2x/4x subtick, equivalent OC, coil OC 4x/8x)"
keys = [ "getmodifier", "heatingcoiloc", "parallel", "perfect overclock", "subtickparalleloc", "ulv penalty" ]
updated = "2026-10-19 20:08:34"

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
Why not patch the constants: the 4 logic constants are static final, and the old OverclockingPatcher swapped them with Unsafe (dropped). @Overwrite only, the old project's approach. getModifier always computes parallels and has no ULV penalty. subTickParallelOC applies to every subtick logic, not only the replacements. heatingCoilOC ignores coil temperature (old behaviour). Gregified machines use their own logic (gregification.modifiers). Switch: oc.toggle.