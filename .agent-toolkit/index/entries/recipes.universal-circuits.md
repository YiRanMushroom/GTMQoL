+++
title = "Universal circuits: one <tier>_universal_circuit per tier, tagged gtceu:circuits/<tier>, made from any circuit"
keys = [ "ae2 pattern", "circuit conversion", "circuits.universalcircuits" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/circuit/UniversalCircuits.java"
snippet = ".item(VN[tier].toLowerCase(Locale.ROOT) + \"_universal_circuit\", Item::new)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "RECIPE_TYPE.recipeBuilder(GTMQoL.id(\"circuit_conversion_tier_\" + tier))"
+++
Old textures reused. The AE2 pattern encoder prefers them (ae2.universalCircuitEncoding early option). Control circuits (ControlCircuits, ULV-EV) are a separate steam-era feature.