+++
title = "CircuitTags: GT circuit tiers and Mekanism control circuits / infused alloy made interchangeable via item tags"
keys = [ "alloys/advanced", "c:circuits", "mekanismcircuittags", "runtime tags", "ulv" ]
updated = "2026-11-11 08:18:48"
checked = "2026-11-11"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/circuit/CircuitTags.java"
snippet = "public static void addTo(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MixinHelpersMixin.java"
snippet = "CircuitTags.addTo(tagMap);"
+++
Added at runtime (2026-11-11), not datagen: CircuitTags.addTo puts the entries into the tag map GTCEu fills while tags load (MixinHelpersMixin, the hook WireTags uses), only with Mekanism loaded and `circuits.mekanismCircuitTags` on, so the option works and follows /reload. Each pair goes both ways: the common tag (c:circuits/basic ...) takes the GT tier tag, the GT tier tag takes Mekanism's item; both entries optional. Why runtime: rules.direction (static resources only for our own content, GTCEu and AE2). `forge:` instead of `c:` on 1.20.1. Mekanism recipes use both c:alloys/advanced and mekanism:alloys/infused; the latter includes the former.