+++
title = "RecipeLogicMixin: restarting the last recipe dropped lastOriginRecipe, so the next finish went idle"
keys = [ "alwaystrymodifyrecipe", "beehive", "findandhandlerecipe", "marklastrecipedirty", "onrecipefinish" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/RecipeLogicMixin.java"
snippet = "public abstract class RecipeLogicMixin {"
+++
Both branches (identical). Found in a Productive Bees beehive log: a finish whose re-check failed on energy (the wireless hatch checks against what is left of the tick's budget) went idle, restarted through this path, and the next finish went idle again. Added to 1.20.1 with the 2026-10-14 GTCEu bump.