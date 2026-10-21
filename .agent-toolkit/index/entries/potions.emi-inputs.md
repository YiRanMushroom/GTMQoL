+++
title = "Potion fluid inputs in EMI: list a single source fluid (no Uncraftable Potion), plus an \"Any mod's version works\" hint"
keys = [ "atm10", "brewery", "c:potion", "emitags", "immersive engineering", "mapingredienttoentrylist" ]
updated = "2026-10-21 08:13:50"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/FluidRecipeCapabilityMixin.java"
snippet = "@Inject(method = \"mapIngredientToEntryList\", at = @At(\"HEAD\"), cancellable = true)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeViewerWidgetPotionMixin.java"
snippet = "public class GTRecipeViewerWidgetPotionMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionContainers.java"
snippet = "GTMQoLAddon.registrate().addRawLang(ANY_MOD_KEY, \"Any mod's version works\");"
+++
Showing only gtceu's fluid (if present) is the user's choice; display only, the brewery still accepts other mods' potions through the tag. FluidRecipeCapabilityMixin (gtceufix) lists one source fluid; GTRecipeViewerWidgetPotionMixin adds the hint only when the tag has other mods' fluids, flashing green via MUI tooltip auto-update. Both mixins are on both branches (1.20.1: NBT instead of components).