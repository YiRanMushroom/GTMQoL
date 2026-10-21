+++
title = "PB industrial incubator and gene extractor custom recipe logic"
keys = [ "honey treat", "pb_gene_extractor", "pb_incubator", "productive bees", "purity", "spawn egg" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/GeneExtractorLogic.java"
snippet = "enum GeneExtractorLogic implements GTRecipeType.ICustomRecipeLogic {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/IncubatorLogic.java"
snippet = "enum IncubatorLogic implements GTRecipeType.ICustomRecipeLogic {"
+++
Gene extractor: every gene at purity 100 plus the type gene; vanilla bees have no genes on 1.20.1 and read as 0. Incubator treat count from PB's incubatorTreatUse config (default if not loaded yet). Not done (possible later): gene-applying treats in the incubator.