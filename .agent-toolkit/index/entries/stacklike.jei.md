+++
title = "JEI lookup and AE2 pattern encoding for stack-like contents: StackLikeJei, GTRecipeJEICategory mixin (1.20.1 only)"
keys = [ "addingredientsunsafe", "chemicals", "setuprecipeingredients", "skipnotconsumedinputs", "stack-like viewers" ]
updated = "2026-10-19 20:12:49"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/StackLikeJei.java"
snippet = "public static void addIngredients(IRecipeLayoutBuilder builder, GTRecipe recipe) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeJEICategoryStackLikeMixin.java"
snippet = "public class GTRecipeJEICategoryStackLikeMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/EncodePatternTransferHandlerMixin.java"
snippet = "for (var cap : GenericStackLikeRecipeCapability.ALL) gtmqol$addStackLike(consumed, cap, recipe);"
+++
JEI is the 1.20.1 dev viewer. gtceu's setupRecipeIngredients lists only items/fluids; the mixin (TAIL, full descriptor not the generic bridge) adds one slot per stack-like content. Displayed slots are MUI's separate ones. AE2 encodes from JEI slots, AppMek's JEI converter handles chemicals; the encoding mixin (skip chance-0 inputs) assumes slot order items, fluids, then stack-like and gives up if counts differ. With EMI loaded gtceu skips its JEI categories.