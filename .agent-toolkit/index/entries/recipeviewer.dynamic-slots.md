+++
title = "DynamicRecipeSlots.ANY: per-recipe recipe viewer slots; GT's own fixed grid hides contents past the max"
kind = "code"
keys = [ "createcapabilityuilayout", "emi size", "gtrecipeviewerwidget", "max outputs", "recipe viewer", "unlimited outputs" ]
refs = [ ".gtceu-src-1.21/com/gregtechceu/gtceu/api/recipe/gui/GTRecipeViewerWidget.java", ".gtceu-src-1.21/com/gregtechceu/gtceu/api/recipe/gui/RecipeViewerCapabilityLayoutBuilder.java", ".mui-src-1.21/brachy/modularui/integration/emi/recipe/ModularUIEmiRecipe.java" ]
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
Built 2026-11-12 on both branches (the user: per-recipe slots, and a value meaning "any size" for inputs and outputs). DynamicRecipeSlots.ANY (256) as a capability's slot count: RecipeTypeLayouts wraps the item, fluid and stack-like recipe viewer layouts of types that use it, and the wrapper builds a 3-wide grid of as many slots as the recipe has (contents plus tick contents), named cap_io_index so GT fills them; no slots for a recipe without that kind. The recipe comes from GTRecipeViewerWidgetLayoutMixin (RecipeViewerWidgetAccess.gtmqol$recipe, baseRecipe is private); that mixin is apart from GTRecipeViewerWidgetMixin because GTMQoLMixinPlugin drops the latter with the overclocking overhaul off. Gregification.slots makes every non-zero count of a multiblock type ANY (single blocks keep real counts and throw on ANY, it would size their inventories); Mekanism's chemical counts are ANY too. Facts behind it: GT makes the viewer grid from the type's max and skips contents without a slot; GTRecipeBuilder only warns past the max; the max also sizes single-block inventories (MachineDefinition.getInputSize/getOutputSize) and gates hatches (> 0); EMI (ModularUIEmiRecipe) and JEI 1.20 (up to GT's 250x250) size each recipe from its widget; GTEmiRecipe getInputs/getOutputs read every content.