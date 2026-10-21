+++
title = "GTRecipeBuilderMixin + GTEmiRecipeMixin: keep data components on fluid outputs (potion fluids, EMI recipe lookup)"
keys = [ "brewery", "datacomponentfluidingredient", "getoutputs", "outputfluids", "sizedfluidingredient" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/GTRecipeBuilderMixin.java"
snippet = "public class GTRecipeBuilderMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/GTEmiRecipeMixin.java"
snippet = "public class GTEmiRecipeMixin {"
+++
GTRecipeBuilderMixin is 1.21 only (1.20.1's FluidIngredient.of keeps NBT). GTEmiRecipeMixin is on both branches (1.20.1 passes NBT). Inputs already keep components via RecipeHelper.makeFluidIngredient. Related potion work: see the potion entries.