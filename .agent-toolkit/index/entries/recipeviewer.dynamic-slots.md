+++
title = "DynamicRecipeSlots.ANY: per-recipe recipe viewer slots; GT's own fixed grid hides contents past the max"
kind = "code"
keys = [ "emi size", "gtrecipeviewerwidget", "jei size", "max outputs", "recipe viewer", "unlimited outputs" ]
updated = "2026-11-13 03:41:22"
checked = "2026-11-13"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ui/DynamicRecipeSlots.java"
snippet = "public static final int ANY = 256;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java"
snippet = "static int slots(ForeignMachineType type, int count) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeViewerWidgetLayoutMixin.java"
snippet = "private void gtmqol$placeGhostSlots(CallbackInfoReturnable<Flow> cir) {"
+++
Same as the 1.21 entry (details there): ANY as a slot count makes RecipeTypeLayouts' wrapper build each recipe's own 3-wide grid for items, fluids and stack-like caps; the recipe comes from GTRecipeViewerWidgetLayoutMixin (apart from the OC mixin GTMQoLMixinPlugin can drop); Gregification.slots makes a multiblock type's non-zero counts ANY (single blocks throw on it), Mekanism's chemical counts are ANY. JEI sizes each recipe up to GT's 250x250 (ModularUIJeiCategory.getHeight(recipe)).