+++
title = "PB industrial breeder: two caged parents + food + empty cage -> caged baby, XOR-chanced offspring"
keys = [ "breeding chamber", "pb_breeder", "productive bees" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/BreederLogic.java"
snippet = "enum BreederLogic implements GTRecipeType.ICustomRecipeLogic {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/BreederUI.java"
snippet = "private static final String[] OUTPUT_GRID = { \"ssss\", \"ssss\", \"ssss\", \"ssss\" };"
+++
16 output slots because gtceu wants room for every chanced output. Baby genes = type defaults improved by the parents'. One EMI recipe per parent pair.