+++
title = "GTEmiRecipeMixin: keep NBT on fluid outputs in EMI, so R / click on a potion fluid finds its recipe"
keys = [ "1.21", "brewery", "emistack", "getoutputs" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/GTEmiRecipeMixin.java"
snippet = "return EmiStack.of(fluid, stack.getTag(), amount);"
+++
Client mixin. On 1.20.1 only the EMI half is needed: GTCEu 1.20's FluidIngredient keeps NBT on the recipe output itself. 1.21.1 has the same mixin with data components, plus a GTRecipeBuilderMixin for the output ingredient (not on 1.20.1).