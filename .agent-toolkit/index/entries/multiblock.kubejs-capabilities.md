+++
title = "KubeJS plugin binds the block and ghost capabilities; KJSRecipeKeyEvent gives gtceu's KubeJS 6 schema their components"
keys = [ "binding", "block capability", "blockproviderrecipecapability", "contentjs", "crystal_growth", "ghostingredientrecipecapability", "kubejs.plugins.txt" ]
updated = "2026-10-21 08:30:51"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/kubejs/GTMQoLKubeJSPlugin.java"
snippet = "public class GTMQoLKubeJSPlugin extends KubeJSPlugin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/kubejs/GTMQoLKubeJSPlugin.java"
snippet = "public static void registerRecipeKeys(KJSRecipeKeyEvent event) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/kubejs/GhostIngredientComponent.java"
snippet = "public final class GhostIngredientComponent implements RecipeComponent<GhostIngredient<?>> {"
+++
1.20.1 (KubeJS 6): gtceu's KubeJS only writes registered capabilities, so the plugin's KJSRecipeKeyEvent listener registers ContentJS(BlockComponent.INPUT/OUTPUT) for the block capability and GhostIngredientComponent (1.20.1 only; takes stacks, item ids, Fluid.of(...), saved JSON) for ghosts. 1.21 needs neither (its schema serializes any capability through the codec). Usage: event.recipes.gtmqol.crystal_growth(id).input(BlockProviderRecipeCapability.CAP, 'minecraft:budding_amethyst').