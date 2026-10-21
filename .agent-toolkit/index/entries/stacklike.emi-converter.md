+++
title = "EMI slots and lookup for chemicals: ChemicalEmiConverter over JEMI stacks, GTEmiRecipe inputs/outputs mixin"
keys = [ "1.21", "emistackconverter", "jemistack", "jemiutil", "pattern encoding", "r/u", "stack-like viewers" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/client/GTMQoLClient.java"
snippet = "if (ModList.get().isLoaded(\"emi\") && ModList.get().isLoaded(\"jei\") && ModList.get().isLoaded(\"mekanism\")) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/mekanism/ChemicalEmiConverter.java"
snippet = "public final class ChemicalEmiConverter implements EmiStackConverter.Converter<ChemicalStack> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTEmiRecipeStackLikeMixin.java"
snippet = "public abstract class GTEmiRecipeStackLikeMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/StackLikeEmi.java"
snippet = "public static void addInputs(GTRecipe recipe, List<EmiIngredient> inputs) {"
+++
Mek 10.4 has no EMI integration; chemicals exist in EMI only via JEMI (JEI installed too). The converter wraps JemiUtil.getStack (EMI internals, not API) and sets the EMI amount (AE2 encoding reads it); convertFrom unwraps JemiStack.ingredient; EMPTY while JemiPlugin.runtime is null. EMI without JEI: no chemical slots, lookup or encoding. EMI is only compileOnly here (dev viewer is JEI, stacklike.jei). 1.21 uses Mek's own IMekanismEmiHelper.