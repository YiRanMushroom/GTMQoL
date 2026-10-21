+++
title = "overclocking.enableMultiTierSkipping: multiblock max voltage = summed energy hatch voltage, not tier+1"
keys = [ "amperage", "getmaxvoltage", "vex" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/WorkableElectricMultiblockMachineMixin.java"
snippet = "private long gtmqol$multiTierSkip(long[] array, int index, Operation<Long> original) {"
+++
gtmqol.yaml option, default true. Wraps the `return V[...]` of upstream's "several hatches at the highest tier -> tier + 1" branch.