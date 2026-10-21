+++
title = "WireTags: GT 1x wires added to c:wires/<material> at runtime via MixinHelpers.generateGTDynamicTags (one-way)"
keys = [ "kubejs materials", "wiregtsingle" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/tag/WireTags.java"
snippet = "public static void addTo(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MixinHelpersMixin.java"
snippet = "@Inject(method = \"generateGTDynamicTags\", at = @At(\"TAIL\"))"
+++
Always on, both branches (`forge:` on 1.20.1). Runtime (not datagen) so KubeJS materials get it. generateGTDynamicTags is @ApiStatus.Internal: recheck after a GTCEu bump. One-way on purpose: other mods' c:wires recipes take GT wires, GT recipes still take GT wires only; the user rejected adding a tag path to the prefix (would make GT recipes accept any mod's wire). GT 1x wire = half an ingot, like other mods' wires.