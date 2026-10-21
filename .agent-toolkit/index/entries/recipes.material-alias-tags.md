+++
title = "MaterialAliasTags: other mods' spellings (aluminum, plutonium) as tags including GT's material tags"
keys = [ "1.21", "aliases", "aluminium", "datagen", "plutonium_239" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/tag/MaterialAliasTags.java"
snippet = "public static final Map<String, String> ALIASES = Map.of("
+++
Always on, datagen'd (runData). Covers item, block and fluid tags (forge: namespace); only tags whose path contains the material name. GT's own material tags are runtime, so they are added as optional tags. ALIASES is reused by WireTags (recipes.wire-tags); on 1.21.1 also by MI gregification.