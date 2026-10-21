+++
title = "Jade: stack-like recipe outputs of the running recipe (StackLikeRecipeOutputProvider)"
keys = [ "chemicals", "stack-like viewers", "wailaplugin" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/jade/StackLikeRecipeOutputProvider.java"
snippet = "public class StackLikeRecipeOutputProvider extends MachineTraitProvider<RecipeLogic, CompoundTag> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/jade/GTMQoLJadePlugin.java"
snippet = "public class GTMQoLJadePlugin implements IWailaPlugin {"
+++
Priority BODY + 1 so it runs after gtceu's RecipeOutputProvider. Outputs only, like gtceu. Names from GenericStackLikeType.displayName and GenericStackLikeRecipeCapability.describe.