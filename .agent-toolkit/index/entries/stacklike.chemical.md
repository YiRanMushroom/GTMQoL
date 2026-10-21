+++
title = "ChemicalStackLike: Mekanism 10.4 chemicals (all four kinds) as one recipe capability gtmqol:chemical"
keys = [ "1.21", "boxedchemicalstack", "chemicaltype", "gas", "infuse", "pigment", "slurry", "sort index", "stack-like core" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (ModList.get().isLoaded(\"mekanism\")) ChemicalStackLike.init();"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/mekanism/ChemicalStackLike.java"
snippet = "CAP = GenericStackLikeRecipeCapability.register(TYPE, 0xFF9C5ED6, 4, \"Chemical\");"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/mekanism/ChemicalStackLike.java"
snippet = "private static final Codec<ChemicalStackIngredient<?, ?>> INGREDIENT_CODEC = RecordCodecBuilder.create(i -> i"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/mekanism/TaggedChemicalStackIngredientAccessor.java"
snippet = "public interface TaggedChemicalStackIngredientAccessor {"
+++
Gas/InfuseType/Pigment/Slurry share no generic type: S = ChemicalStack<?>, raw casts, dispatch on ChemicalType. Mek 10.4 has no codecs (unclean, accepted): ingredient = Mek JSON via ExtraCodecs.JSON + chemicalType field (JSON can't tell a tag's kind); stack via BoxedChemicalStack NBT. withAmount rebuilds via the creator; tags need the accessor (mekanism/ mixins apply only with Mek). 1.21 has a single Chemical type.