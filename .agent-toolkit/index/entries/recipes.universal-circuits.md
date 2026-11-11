+++
title = "Universal circuits: one <tier>_universal_circuit per tier, tagged gtceu:circuits/<tier>, made from any circuit"
keys = [ "ae2 pattern", "circuit conversion", "circuits.universalcircuits" ]
updated = "2026-11-11 07:38:56"
checked = "2026-11-11"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/circuit/UniversalCircuits.java"
snippet = "GTMQoLAddon.optionalItemTag(CustomTags.CIRCUITS_ARRAY[tier], name);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/circuit/UniversalCircuits.java"
snippet = "MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id(\"circuit_conversion_tier_\" + tier))"
+++
Conversion: any circuit of the tier + circuit 5 in the magical assembler, 1 tick, 1 EU/t. The AE2 pattern encoder prefers them (ae2.universalCircuitEncoding early option, EncodingHelperMixin). Control circuits (ControlCircuits, circuits.controlCircuits) are a separate steam-era feature.