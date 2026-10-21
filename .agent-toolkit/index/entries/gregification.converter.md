+++
title = "ForeignRecipeConverter: one foreign recipe to 0..n GT recipes; Single form and prepare() hook"
keys = [ "gregification core", "output" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/ForeignRecipeConverter.java"
snippet = "void convert(RecipeHolder<?> holder, Output output);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/ForeignRecipeConverter.java"
snippet = "static ForeignRecipeConverter single(Single converter) {"
+++
Each output.add fills a fresh builder; returning false drops just that one, so a converter that bails halfway leaves nothing. Multi-recipe users: Mek rotary (one per direction), Create fan (one per kind x catalyst sort), PB centrifuge (comb + comb block). prepare() is used by ghost.mark-covered-recipes.