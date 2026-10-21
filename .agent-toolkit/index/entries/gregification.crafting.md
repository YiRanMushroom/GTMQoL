+++
title = "How gregified machines are crafted: counterpart + kept catalyst, hammer/magical assembler, or custom Crafting"
keys = [ "addrecipes", "catalyst_shapeless", "gregification core" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/CatalystShapelessRecipe.java"
snippet = "SERIALIZERS.register(\"catalyst_shapeless\", Serializer::new);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "public static void addRecipes(Consumer<FinishedRecipe> provider) {"
+++
Catalyst set (PB blocks): shapeless counterpart + catalyst, catalyst stays in the grid; the recipe is a FinishedRecipe with an extra "catalyst" JSON field. Catalyst null (Mek): hammer over counterpart + magical assembler (circuit 5). withCrafting replaces those (Create). Two machines must not share a counterpart (identical recipes): craft the second from the first, e.g. mek_chemical_converter from mek_chemical_oxidizer. Missing counterpart/catalyst items are skipped with a debug log.