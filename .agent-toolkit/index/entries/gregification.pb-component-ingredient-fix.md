+++
title = "gtceufix ItemDataComponentMapIngredientMixin: recipe lookup for partial component ingredients (PB configurable combs)"
keys = [ "datacomponentingredient", "productive bees", "productivebees:component" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/ItemDataComponentMapIngredientMixin.java"
snippet = "private boolean gtmqol$matchPartialComponents(boolean equal, Object obj) {"
+++
Applies to every recipe type; symptom was PB's own Mek oxidizer recipes never found (wasted radioactive comb).