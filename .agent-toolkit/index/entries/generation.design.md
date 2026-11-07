+++
title = "Runtime generation design: registry vs. published JSON, GTCEu dynamic pack lifecycle, unbuilt server-data writer plan"
keys = [ "collision policy", "gtdynamicdatapack", "gtdynamicresourcepack", "src/generated/resources" ]
updated = "2026-11-07 09:53:07"
checked = "2026-11-07"

[[items]]
name = "runtime-generation"
value = "Queue of machine assets, lang and callbacks, replayed into GTDynamicResourcePack on every reload"
path = "src/main/java/com/yiran/minecraft/gtmqol/api/generation/RuntimeGeneration.java"
snippet = "public static void generateAll() {"

[[items]]
name = "generate-machine"
value = "Same model dispatch as GTCEu's KJSCallWrapper#generateAssetJsons, item model parent block/machine/<id>, lang"
path = "src/main/java/com/yiran/minecraft/gtmqol/api/generation/RuntimeGeneration.java"
snippet = "private static void generateMachine(RuntimeBlockstateProvider provider, MachineBuilder<?, ?, ?> builder,"

[[items]]
name = "lang-first-wins"
value = "First value per key wins"
path = "src/main/java/com/yiran/minecraft/gtmqol/api/generation/RuntimeGeneration.java"
snippet = "public static void addLanguageEntry(String namespace, String language, String key, String value) {"

[[items]]
name = "dynamic-resources-listener"
value = "Our RegisterDynamicResourcesEvent listener"
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "private void onRegisterDynamicResources(RegisterDynamicResourcesEvent event) {"

[[items]]
name = "dynamic-tags"
value = "Tags added at the tail of GTCEu's generateGTDynamicTags"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MixinHelpersMixin.java"
snippet = "@Inject(method = \"generateGTDynamicTags\", at = @At(\"TAIL\"))"
+++
Goal: machine content without Gradle datagen or committed `src/generated/resources`.

Boundary: normal mod init registers items/blocks/machines/recipe types; runtime generation only publishes their JSON (client resources via `RuntimeGeneration`, recipes via `IGTAddon.addRecipes`, see recipes.sources).

Upstream lifecycle (GTCEu v8 1.20.1, checked in the 8.0.0-20261013 sources jar): `CommonProxy.registerPackFinders` (`AddPackFindersEvent`) clears and mounts `GTDynamicResourcePack` (client) and, for server data, clears `GTDynamicDataPack` and runs all recipe additions (addons' `addRecipes` included) into it. `ModelManagerMixin` fires `RegisterDynamicResourcesEvent` in mod order at the head of `ModelManager.reload`, i.e. right before models are baked - the only point where adding to the client pack still takes effect. Hence `RuntimeGeneration` keeps its queue and replays it on every reload.

What a machine needs: single block = registered definition + block model + blockstate + item model (parent `block/machine/<id>`) + lang; multiblock = the same for the controller (structure stays a Java pattern). `generateMachine` uses the same dispatch as GTCEu's `MachineBuilder.KJSCallWrapper#generateAssetJsons` (can't be called: its signature needs a KubeJS type). Lang is one file per namespace+language (a second write would replace the first), first value per key wins.

Not built (plan from the old design doc): a generic server-data JSON writer (tags, advancements) into `GTDynamicDataPack` with one collision policy - same path + same JSON accept, different JSON log an error and fail fast, cleared on reload by GTCEu's pack lifecycle. Today tags are datagen'd or added via the generateGTDynamicTags mixin (recipes.wire-tags).