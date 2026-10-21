+++
title = "Early game: casing + chest/glass buses and hatches, vanilla sand->glass kept, any sapling -> rubber sapling"
keys = [ "hardglassrecipes", "keepvanillatnt", "recipes.earlygame", "steam tweaks", "tnt", "ulv" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/EarlyGameRecipes.java"
snippet = "public final class EarlyGameRecipes {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/RecipeRemovalMixin.java"
snippet = "public class RecipeRemovalMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssembler.java"
snippet = "RECIPE_TYPE.recipeBuilder(GTMQoL.id(\"rubber_sapling\"))"
+++
The rubber sapling recipe (magical assembler: any sapling + sticky resin) is always on with the magical assembler.