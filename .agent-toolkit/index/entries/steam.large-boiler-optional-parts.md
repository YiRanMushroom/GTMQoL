+++
title = "Large boilers: muffler and maintenance hatch optional (setExactLimit -> setMaxGlobalLimited)"
keys = [ "early game", "optionallargeboilerparts", "registerlargeboiler", "steam tweaks", "steamtweaks.optionallargeboilerparts" ]
updated = "2026-11-07 10:20:22"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTMachineUtilsMixin.java"
snippet = "private static MultiPredicate gtmqol$optionalBoilerParts(MultiPredicate predicate, int limit,"
+++
Config steamTweaks.optionalLargeBoilerParts (default on, restart). GTMachineUtilsMixin wraps the only two setExactLimit calls in the registerLargeBoiler pattern lambda (selected by regex, require = 2) with setMaxGlobalLimited, so large boilers can be built before LV.