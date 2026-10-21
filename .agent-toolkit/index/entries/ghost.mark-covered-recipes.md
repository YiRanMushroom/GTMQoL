+++
title = "MarkCoveredRecipes: gregification wrapper adding a ghost mark only to recipes another recipe's inputs would also run"
keys = [ "assembler", "create saw", "foreignrecipeconverter", "saw" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/MarkCoveredRecipes.java"
snippet = "public final class MarkCoveredRecipes implements ForeignRecipeConverter {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/CreateGregification.java"
snippet = "return new MarkCoveredRecipes(CreateGregification::convertProcessing, CreateGregification::itemInputs,"
+++
For gregified machines that choose recipes by hand or filter; on 1.20.1 only Create's saw uses it (1.21 also uses it for MI's assembler; MI is 1.21 only). prepare() runs once per reload with all recipes of the foreign type, before converting. The mark is the recipe's main output. Counts are not compared. The type needs setMaxSize(IO.IN, GhostIngredientRecipeCapability.CAP, 1).