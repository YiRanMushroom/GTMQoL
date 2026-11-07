+++
title = "KubeJS plugin binds BlockProviderRecipeCapability and GhostIngredientRecipeCapability for scripts"
keys = [ "binding", "block capability", "crystal_growth", "kubejs.plugins.txt" ]
updated = "2026-11-07 10:16:37"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/kubejs/GTMQoLKubeJSPlugin.java"
snippet = "public class GTMQoLKubeJSPlugin implements KubeJSPlugin {"
+++
1.21: gtceu's schema serializes any capability through its codec (Content.codec calls the serializer's `of`, which also takes block/item ids). 1.20.1 (KubeJS 6): GTMQoLKubeJSPlugin.registerRecipeKeys (KJSRecipeKeyEvent listener added in GTMQoL) registers ContentJS(BlockComponent.INPUT/OUTPUT) for the block capability, since gtceu's KubeJS only writes registered capabilities. GTCEu registers a schema for every GT recipe type, so event.recipes.gtmqol.crystal_growth(id) exists.