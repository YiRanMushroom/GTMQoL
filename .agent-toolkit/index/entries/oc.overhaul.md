+++
title = "Overclocking overhaul: OverclockingLogic @Overwrites (4x/4x and 2x/4x subtick, equivalent OC, coil OC 4x/8x)"
keys = [ "getmodifier", "heatingcoiloc", "parallel", "perfect overclock", "subtickparalleloc", "ulv penalty" ]
updated = "2026-11-10 07:47:07"
checked = "2026-11-10"

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
Why not patch the constants: the 4 logic constants are static final; the old OverclockingPatcher swapped them with Unsafe (dropped). @Overwrite only. Overclocking.replace maps all four GTCEu logics, subtick or not, to subtick STANDARD 4x/4x or PERFECT 2x/4x. getModifier computes parallels for every logic unless shouldParallel is false; no ULV penalty. subTickParallelOC applies to every subtick logic. Coil OC (user's design, 2026-11-08): every OC 4x EU/t; one OC per 900K above the recipe gives 8x speed, the rest 4x, the 8x ones first; equivalent OC via Overclocking.equivalentOC (shared with subTickParallelOC); gtceu's 0.95 EU/t per 900K stays in ebfOverclock. Before it was 8x for every OC regardless of temperature, carried over from the old version. Parallel limits passed to ParallelLogic stay at most Overclocking.MAX_PARALLELS (2e9), never Integer.MAX_VALUE: getMaxByInput starts its minimum at Integer.MAX_VALUE and reports "no capabilities: import" if it is still that, which happened with every input allowing the limit (gregified rotary condensentrator, UHV max amps, >2.1G mB fluid; fixed 2026-11-10 in GregificationModifiers.overclock and AbsoluteParallel.upTo). Gregified: gregification.modifiers. Switch: oc.toggle.