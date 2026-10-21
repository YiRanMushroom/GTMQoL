+++
title = "LegacyCapabilityAliases: PB's gtceu 1.x ore-bee recipes (\"item\", \"eu\" keys) load in gtceu v8"
keys = [ "gtregistry", "minecraft:item", "productive bees", "recipe_capabilities", "remap", "unknown registry key" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/productivebees/LegacyCapabilityAliases.java"
snippet = "if (!registry.containsKey(alias)) registry.remap(alias, id);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (ModList.get().isLoaded(\"productivebees\")) LegacyCapabilityAliases.register();"
+++
Registered whenever PB is loaded. 1.20.1 GTRegistry has no addAlias: remap() adds minecraft:<path> to keyToValue as a second key (valueToKey unchanged). Checked in gtceu sources (2026-10-20): harmless, getKey/codec unaffected and the only iterations (RecipeLogic, DummyRecipeUtils) fill maps keyed by capability.