+++
title = "PotionCanningLogic: representative canner recipes (bottling/draining every potion) for the recipe viewer"
keys = [ "buildrepresentativerecipes", "emi", "jei" ]
updated = "2026-11-08 19:27:43"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionCanningLogic.java"
snippet = "public enum PotionCanningLogic implements GTRecipeType.ICustomRecipeLogic {"
+++
A recipe logic runner added to the canner type at setup (after gtceu's CannerLogic), only for buildRepresentativeRecipes: gtceu's CannerLogic fills/drains at runtime but shows nothing. Every potion as glass bottle <-> PotionFluidHelper fluid, also as splash/lingering (potions.splash-lingering); no water bottling (glass bottle takes potion fluids only). Ids get a "/" prefix so the viewer treats them as synthetic.