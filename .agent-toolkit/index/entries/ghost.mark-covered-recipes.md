+++
title = "MarkCoveredRecipes: gregification wrapper adding a ghost mark only to recipes another recipe's inputs would also run"
keys = [ "assembler", "create saw", "foreignrecipeconverter", "mi assembler", "saw" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/MarkCoveredRecipes.java"
snippet = "public final class MarkCoveredRecipes implements ForeignRecipeConverter {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/create/CreateGregification.java"
snippet = "return new MarkCoveredRecipes(CreateGregification::convertProcessing, CreateGregification::itemInputs,"
+++
For gregified machines that choose recipes by hand or filter (Create's saw, MI's assembler). prepare() runs once per reload with all recipes of the foreign type, before converting. The mark is the recipe's main output. The type needs setMaxSize(IO.IN, GhostIngredientRecipeCapability.CAP, 1).