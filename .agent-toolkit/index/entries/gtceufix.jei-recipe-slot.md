+++
title = "JeiRecipeSlotMixin: restore RecipeSlot fields ModularUI's accessor targets, so JEMI recipes work on JEI 19.46+"
keys = [ "allingredients", "atm10", "create", "displayingredients", "gtmqol.jeifix.mixins.json", "recipeslotaccessor" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/jei/JeiRecipeSlotMixin.java"
snippet = "public class JeiRecipeSlotMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/JeiRecipeSlotFixPlugin.java"
snippet = "public class JeiRecipeSlotFixPlugin extends GTMQoLMixinConfigPlugin {"
+++
1.21 only. Seen with ATM10's JEI 19.57: every recipe that EMI shows through JEMI broke (Create showed nothing).