+++
title = "Potion fluids in the recipe viewer: inputs list a single source fluid (no Uncraftable Potion), outputs keep NBT"
keys = [ "any mod", "brewery", "create", "hint", "immersive engineering", "mapingredienttoentrylist" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionContainers.java"
snippet = "public static final String ANY_MOD_KEY = \"gtmqol.gui.content.any_mod_fluid\";"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/FluidRecipeCapabilityMixin.java"
snippet = "public class FluidRecipeCapabilityMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/GTEmiRecipeMixin.java"
snippet = "public class GTEmiRecipeMixin"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeViewerWidgetPotionMixin.java"
snippet = "private static boolean gtmqol$anyMod(FluidIngredient ingredient) {"
+++
FluidRecipeCapabilityMixin and GTEmiRecipeMixin are gtceufix (remove when gtceu fixes them). Showing only gtceu's fluid (if any) is the user's choice; display only, the brewery still accepts other mods' potions via the tag. GTEmiRecipeMixin: EMI R on a potion found nothing because outputs dropped NBT. GTRecipeViewerWidgetPotionMixin adds the flashing "Any mod's version works" hint (ported 2026-10-20, same as 1.21).