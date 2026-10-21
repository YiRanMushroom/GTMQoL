+++
title = "GenericStackLikeRecipeCapability: registration, recipe lookup map ingredients, parallel limits (mirrors gtceu fluids)"
keys = [ "describe", "register", "registermapingredient", "setmaxsize", "stack-like core" ]
updated = "2026-10-19 20:08:35"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeRecipeCapability.java"
snippet = "public static <S, I> GenericStackLikeRecipeCapability<S, I> register(GenericStackLikeType<S, I> type,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeMapIngredient.java"
snippet = "public final class GenericStackLikeMapIngredient extends AbstractMapIngredient {"
+++
Use in recipe builders with builder.input(CAP, ingredient) / builder.output(CAP, ingredient); slot counts via GTRecipeTypeBuilder.setMaxSize(IO, CAP, n) (gregification: ForeignMachineType.withRecipeType). A tag ingredient becomes one lookup node per matched type. ALL is what the EMI defaults (stacklike.emi-display) iterate.