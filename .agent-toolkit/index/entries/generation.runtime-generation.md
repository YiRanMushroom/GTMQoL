+++
title = "RuntimeGeneration: machine models/blockstates/lang published to GTCEu's dynamic resource pack at runtime, no datagen"
keys = [ "addlanguageentry", "dynamicallygenerated", "gtdynamicresourcepack", "idynamicgenerationhandler", "ongenerate", "registerdynamicresourcesevent", "runtimeblockstateprovider" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "private void onRegisterDynamicResources(RegisterDynamicResourcesEvent event) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/api/generation/GTMQoLMachineBuilder.java"
snippet = "public GTMQoLMachineBuilder<D, M> dynamicallyGenerated(boolean dynamicGenerated) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/api/generation/RuntimeGeneration.java"
snippet = "public static void generateAll() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MachineBuilderMixin.java"
snippet = "private void gtmqol$queueDynamicGeneration(CallbackInfoReturnable<MachineDefinition> cir) {"
+++
Only publishes JSON; items/blocks/machines are still registered in the normal Forge/GTCEu lifecycle. A multiblock's structure stays Java (pattern). Recipes don't go through this (recipes.sources); there is no server-data writer. The queue is replayed on every resource reload. GTMQoLMultiblockBuilder has the same dynamicallyGenerated; other builders opt in by casting to IDynamicGenerationHandler. Item tags, mold models, static lang still use runData.