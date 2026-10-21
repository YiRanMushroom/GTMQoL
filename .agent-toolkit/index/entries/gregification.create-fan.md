+++
title = "Create encased fan: every fan_processing_type kind (incl. addons), catalysts from fan_processing_catalysts tags"
keys = [ "_manual_only", "bulk washing", "create gregification", "dragons plus", "freezing", "haunting", "sanding" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/CreateGregification.java"
snippet = "private static void convertFan(Recipe<?> foreign, ForeignRecipeConverter.Output output) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/FanProcessing.java"
snippet = "static List<RecipeType<?>> recipeTypes() {"
+++
Kinds read from CreateBuiltInRegistries.FAN_PROCESSING_TYPE at common setup (withProxies). One GT recipe per kind x catalyst sort (items or a 1000 mB fluid tag), catalysts never consumed, cached per kind until LoadingTags change. ULV 30 s, OC_NON_PERFECT + unlimited AbsoluteParallel. Dragons Plus handled by id, no dependency; its coloring has no recipe type and is left out (info log).