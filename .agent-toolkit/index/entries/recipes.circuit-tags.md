+++
title = "CircuitTags: GT circuit tiers and Mekanism control circuits / infused alloy made interchangeable via item tags"
keys = [ "1.21", "alloys/advanced", "circuits.mekanismcircuittags", "datagen", "forge:circuits", "ulv" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/tag/CircuitTags.java"
snippet = "private static final Pair[] PAIRS = {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (config.circuits.mekanismCircuitTags) CircuitTags.init();"
+++
Static datagen JSON (needs runData; datagen always sees every feature on), so the circuits.mekanismCircuitTags toggle does not remove the tags at runtime: known limitation, left on purpose. Uses forge: tags (1.21.1 uses c:). Mekanism recipes use both forge:alloys/advanced and mekanism:alloys/infused; the latter includes the former.