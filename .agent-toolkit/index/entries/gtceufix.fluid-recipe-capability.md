+++
title = "FluidRecipeCapabilityMixin: NBT fluid inputs (potions) listed as one source fluid, so EMI shows no Uncraftable Potion"
keys = [ "1.21", "brewery", "canner", "fluidstacklist", "mapingredienttoentrylist" ]
updated = "2026-10-21 08:25:55"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/FluidRecipeCapabilityMixin.java"
snippet = "private static void gtmqol$keepNbt(FluidIngredient ingredient, CallbackInfoReturnable<FluidEntryList> cir) {"
+++
Both branches (1.21.1 checks DataComponentFluidIngredient instead of NBT). Common mixin config. Ranged (IRangedIngredient) inputs are left alone. The returned stack is a copy because getStacks() is cached by the ingredient. Both branches also have GTRecipeViewerWidgetPotionMixin, which adds the "any mod's version works" hint to such input slots.