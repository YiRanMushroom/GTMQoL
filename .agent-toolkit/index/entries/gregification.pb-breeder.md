+++
title = "PB industrial breeder: two caged parents + food + empty cage -> caged baby, XOR-chanced offspring"
keys = [ "breeding chamber", "pb_breeder", "productive bees" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/BreederLogic.java"
snippet = "enum BreederLogic implements GTRecipeType.ICustomRecipeLogic {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/BreederUI.java"
snippet = "private static final String[] OUTPUT_GRID = { \"ssss\", \"ssss\", \"ssss\", \"ssss\" };"
+++
16 output slots because gtceu wants room for every chanced output. Parents are never used up; one random offspring when there are several. One recipe-viewer recipe per parent pair.