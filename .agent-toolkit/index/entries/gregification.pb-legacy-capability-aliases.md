+++
title = "LegacyCapabilityAliases: PB's gtceu 1.x ore-bee recipes (\"item\", \"eu\" keys) load in gtceu v8"
keys = [ "addalias", "minecraft:item", "productive bees", "recipe_capabilities", "unknown registry key" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/productivebees/LegacyCapabilityAliases.java"
snippet = "if (!registry.containsKey(alias)) registry.addAlias(alias, id);"
+++
Registered whenever PB is loaded, regardless of the gregification config. Without them, e.g. gtceu:alloy_smelter/barite_bee fails with "Unknown registry key ... minecraft:item".