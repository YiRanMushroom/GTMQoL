+++
title = "LoadingTags / GTMaterialTags: reading tags while recipes load, before new tags are bound"
keys = [ "addreloadlistenerevent", "getconditioncontext", "gregification core", "onchange", "tags during reload", "tagsupdatedevent" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/LoadingTags.java"
snippet = "public static synchronized <T> Map<ResourceLocation, Collection<Holder<T>>> all(Registry<T> registry) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/GTMaterialTags.java"
snippet = "public static synchronized List<TagKey<Item>> of(Item item) {"
+++
User approved LoadingTags as a shared helper. Users: GTMaterialTags (MI item widening, see gregification.mi) and the Create fan catalysts (gregification.create-fan). GTMaterialTags indexes item -> GT material tags (incl. 1x wire tags and MaterialAliasTags spellings) once per reload.