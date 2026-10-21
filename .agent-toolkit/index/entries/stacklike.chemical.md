+++
title = "ChemicalStackLike: Mekanism chemicals as recipe capability gtmqol:chemical (registered when mekanism is loaded)"
keys = [ "chemicalstackingredient", "sort index", "stack-like core" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/mekanism/ChemicalStackLike.java"
snippet = "CAP = GenericStackLikeRecipeCapability.register(TYPE, 0xFF9C5ED6, 4, \"Chemical\");"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (ModList.get().isLoaded(\"mekanism\")) ChemicalStackLike.init();"
+++
Runs in the GTMQoL constructor. 1.20.1 (Mek 10.4) differs a lot, see the 1.20.1 branch's index.