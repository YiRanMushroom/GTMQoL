+++
title = "Jade: stack-like recipe outputs of the running recipe (StackLikeRecipeOutputProvider)"
keys = [ "1.21", "chemicals", "stack-like viewers", "wailaplugin" ]
updated = "2026-10-19 20:12:38"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/jade/GTMQoLJadePlugin.java"
snippet = "public class GTMQoLJadePlugin implements IWailaPlugin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/jade/StackLikeRecipeOutputProvider.java"
snippet = "public class StackLikeRecipeOutputProvider extends MachineTraitProvider<RecipeLogic, CompoundTag> {"
+++
Priority BODY + 1, after gtceu's RecipeOutputProvider; outputs only, like gtceu. Adds its own header only when the recipe has no item/fluid outputs (gtceu prints it otherwise). Names from GenericStackLikeType.displayName / describe. Jade is modCompileOnly + modLocalRuntime; plain NbtOps (1.21 needs registry ops).