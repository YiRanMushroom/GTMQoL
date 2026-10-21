+++
title = "WireTags: GT 1x wires added to forge:wires/<material> at runtime via MixinHelpers.generateGTDynamicTags (one-way)"
keys = [ "1.21", "kubejs materials", "wiregtsingle" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MixinHelpersMixin.java"
snippet = "@Inject(method = \"generateGTDynamicTags\", at = @At(\"TAIL\"))"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/tag/WireTags.java"
snippet = "public static void addTo(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap) {"
+++
Always on (1.21.1 does the same with c:wires). Runtime, not datagen, so KubeJS materials get it; alias spellings (aluminum) too. generateGTDynamicTags is @ApiStatus.Internal: recheck after a GTCEu bump. One-way on purpose: other mods' wire-tag recipes take GT wires, GT recipes still take GT wires only; the user rejected adding the tag to GT's wire prefix (GT recipes would accept any mod's wire). GT 1x wire = half an ingot, like other mods' wires.