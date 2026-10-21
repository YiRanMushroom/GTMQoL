+++
title = "GenericStackLikeRecipeCapability: creation, registry event registration, lookup map ingredients, parallels"
keys = [ "describe", "getdefaultmapingredient", "register", "registerall", "setmaxsize", "stack-like core" ]
updated = "2026-10-19 20:12:38"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeRecipeCapability.java"
snippet = "public static <S, I> GenericStackLikeRecipeCapability<S, I> register(GenericStackLikeType<S, I> type,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeRecipeCapability.java"
snippet = "public static void registerAll(GTCEuAPI.RegisterEvent<ResourceLocation, RecipeCapability<?>> event) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "GenericStackLikeRecipeCapability.registerAll(event);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeMapIngredient.java"
snippet = "public final class GenericStackLikeMapIngredient extends AbstractMapIngredient {"
+++
register (in the mod constructor) only creates the cap + lang and adds it to ALL; registerAll puts ALL into gtceu's registry on the RecipeCapability RegisterEvent. No registerMapIngredient: Mek's ingredient class is an interface, getFrom falls back to getDefaultMapIngredient (one node per matched type for tags). Matching/parallel logic mirrors FluidRecipeCapability. Recipe types: setMaxSize(IO, CAP, n), e.g. ForeignMachineType.withRecipeType in gregification.