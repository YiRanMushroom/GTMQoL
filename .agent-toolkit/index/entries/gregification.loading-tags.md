+++
title = "LoadingTags: reading tags while recipes load, before the new tags are bound"
keys = [ "addreloadlistenerevent", "getconditioncontext", "gregification core", "icondition.icontext", "onchange", "tags during reload", "tagsupdatedevent" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/LoadingTags.java"
snippet = "public static synchronized <T> Map<ResourceLocation, Collection<Holder<T>>> all(Registry<T> registry) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/LoadingTags.java"
snippet = "private static void changed(@Nullable ICondition.IContext newContext) {"
+++
User approved LoadingTags as a shared helper. Reads Forge's ICondition.IContext from AddReloadListenerEvent until TagsUpdatedEvent, then the registries. Only user on 1.20.1: the Create fan catalysts (gregification.create-fan). GTMaterialTags (MI item widening) is 1.21 only, since MI is.