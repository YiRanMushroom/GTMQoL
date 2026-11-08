+++
title = "PotionCanningLogic: representative canner recipes (bottling/draining every potion, per container) for EMI"
keys = [ "lingering", "splash" ]
updated = "2026-11-08 19:26:09"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionCanningLogic.java"
snippet = "public enum PotionCanningLogic implements GTRecipeType.ICustomRecipeLogic {"
+++
A recipe logic runner added to the canner type at setup (after gtceu's CannerLogic), only for buildRepresentativeRecipes; gtceu's logic does the filling and draining at runtime. Both branches. Container variants come from PotionContainers (potions.splash-lingering).