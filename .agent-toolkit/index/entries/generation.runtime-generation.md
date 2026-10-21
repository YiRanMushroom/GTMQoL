+++
title = "RuntimeGeneration: machine models/blockstates/lang published to GTCEu's dynamic resource pack at runtime, no datagen"
keys = [ "addlanguageentry", "dynamicallygenerated", "gtdynamicresourcepack", "idynamicgenerationhandler", "ongenerate", "registerdynamicresourcesevent" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/api/generation/RuntimeGeneration.java"
snippet = "public static void generateAll() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MachineBuilderMixin.java"
snippet = "private void gtmqol$queueDynamicGeneration(CallbackInfoReturnable<MachineEntry<?>> cir) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/api/generation/GTMQoLMachineBuilder.java"
snippet = "public GTMQoLMachineBuilder<D, M> dynamicallyGenerated(boolean dynamicGenerated) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "private void onRegisterDynamicResources(RegisterDynamicResourcesEvent event) {"
+++
Only publishes JSON; items/blocks/machines are still registered in the normal NeoForge/GTCEu lifecycle. A multiblock's structure stays Java (pattern), not a resource. Recipes don't go through this: see recipes.sources. Builders made elsewhere opt in by casting to IDynamicGenerationHandler. Goal: no src/generated/resources needed for machine content (other things like item tags/mold models still use runData).