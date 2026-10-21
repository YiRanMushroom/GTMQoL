+++
title = "EnergyContainerListMixin: O(amperage) hasPrimeFactorGreaterThanTwo replaced by a power-of-two test (77% of a profile)"
keys = [ "energy line", "lag", "performance", "wireless hatch" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/EnergyContainerListMixin.java"
snippet = "cir.setReturnValue(l > 1 && (l & (l - 1)) != 0);"
+++
Both branches (identical). Triggered by our wireless energy hatches' huge amperage.