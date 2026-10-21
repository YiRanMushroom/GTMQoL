+++
title = "Create source: 12 Create-casing multiblocks (millstone, press, mixer, fan, deployer, saw, crafter, SA line...)"
keys = [ "casingpartrender", "connected textures", "create gregification", "create_", "heated basin", "mechanical crafter", "sandpaper polishing" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "gradle.properties"
snippet = "create_version=8amzvn9x"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/CreateGregification.java"
snippet = "public static List<ForeignMachineType> types() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/CreateGregification.java"
snippet = "private static boolean convertProcessing(Recipe<?> foreign, GTRecipeBuilder builder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/MultiblockShape.java"
snippet = "MachineBuilder.ModelInitializer casingParts() {"
+++
8amzvn9x = Create mc1.20.1-6.0.8 (Forge); 1.21 also has Create. Config gregification.create. Create recipes have no energy: LV 5 s, heated MV, superheated HV; sandpaper LV 30 s circuit 1. Mechanical crafter: shapeless + result as ghost input so other shapes are told apart. Saw: ghost.mark-covered-recipes. casingParts draws controller/parts as the Create casing (connected textures). Left out: press/mixer crafting recipes, mechanical crafting grid, potions in spout/basin.