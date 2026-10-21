+++
title = "CircuitTags: GT circuit tiers and Mekanism control circuits / infused alloy made interchangeable via item tags"
keys = [ "alloys/advanced", "c:circuits", "datagen", "ulv" ]
updated = "2026-10-19 20:08:35"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/tag/CircuitTags.java"
snippet = "private static final Pair[] PAIRS = {"
+++
Static datagen JSON (needs runData), so the `circuits.*` config toggles don't remove these tags at runtime: a known limitation, left on purpose. The old version rebound holder sets at TagsUpdatedEvent instead. `forge:` instead of `c:` on 1.20.1. Mekanism recipes use both c:alloys/advanced and mekanism:alloys/infused; the latter includes the former.