+++
title = "Large boilers: muffler and maintenance hatch optional (setExactLimit -> setMaxGlobalLimited)"
keys = [ "early game", "optionallargeboilerparts", "registerlargeboiler", "steam tweaks", "steamtweaks.optionallargeboilerparts" ]
updated = "2026-11-07 10:19:58"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTMachineUtilsMixin.java"
snippet = "private static MultiPredicate gtmqol$optionalBoilerParts(MultiPredicate predicate, int limit,"
+++
GTMachineUtilsMixin wraps the two setExactLimit calls (muffler, maintenance) in gtceu's registerLargeBoiler pattern lambda (regex selector, require = 2) with setMaxGlobalLimited, so large boilers can be built before LV. Config steamTweaks.optionalLargeBoilerParts (default on, restart).