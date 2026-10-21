+++
title = "EnergyContainerListMixin: O(amperage) hasPrimeFactorGreaterThanTwo replaced by a power-of-two test (77% of a profile)"
keys = [ "energy line", "lag", "performance", "wireless hatch" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/EnergyContainerListMixin.java"
snippet = "public abstract class EnergyContainerListMixin {"
+++
Both branches (1.20.1's GTCEu has the same loop up to l / 2 and rebuilds the list every tick). Triggered by our wireless energy hatches' huge amperage.