+++
title = "CannerLogicMixin: representative canner recipes (bottling/draining every potion) for the recipe viewer"
keys = [ "buildrepresentativerecipes", "emi", "jei" ]
updated = "2026-10-19 20:16:06"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/CannerLogicMixin.java"
snippet = "public class CannerLogicMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/CannerLogicMixin.java"
snippet = "GTRecipeTypes.CANNER_RECIPES.addToMainCategory(recipe);"
+++
A feature mixin (in mixins/, not gtceufix/): gtceu's CannerLogic fills/drains at runtime but shows nothing. Every potion as glass bottle <-> PotionFluidHelper fluid, also as splash/lingering (potions.splash-lingering); no water bottling (glass bottle takes potion fluids only). Ids get a "/" prefix so the viewer treats them as synthetic.