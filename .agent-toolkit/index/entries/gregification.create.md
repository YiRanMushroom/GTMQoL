+++
title = "Create source: 12 Create-casing multiblocks (millstone, press, mixer, fan, deployer, saw, crafter, SA line...)"
keys = [ "casingpartrender", "connected textures", "create gregification", "create_", "heated basin", "mechanical crafter", "sandpaper polishing" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/CreateGregification.java"
snippet = "public static List<ForeignMachineType> types() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/CreateGregification.java"
snippet = "private static boolean convertProcessing(RecipeHolder<?> holder, GTRecipeBuilder builder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/MultiblockShape.java"
snippet = "MachineBuilder.ModelInitializer casingParts() {"

[[links]]
path = "gradle.properties"
snippet = "create_version=UjX6dr61"
+++
Both branches (1.21 UjX6dr61 = 6.0.10, 1.20.1 8amzvn9x = 6.0.8). Create recipes have no energy: LV 5 s, heated MV, superheated HV. Mechanical crafter: shapeless + result as ghost input so other shapes are told apart. Saw: ghost.mark-covered-recipes. Left out: what Create does without recipes (press/mixer crafting recipes, mechanical crafting grid, potions in spout/basin).