+++
title = "Potion fluids in the recipe viewer: inputs list a single source fluid (no Uncraftable Potion), outputs keep NBT"
keys = [ "any mod", "brewery", "comparison", "create", "hint", "immersive engineering", "mapingredienttoentrylist", "potionemi", "subtype interpreter" ]
updated = "2026-11-08 22:04:58"
checked = "2026-11-08"

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
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionFluidHint.java"
snippet = "public final class PotionFluidHint {"
+++
FluidRecipeCapabilityMixin and GTEmiRecipeMixin are gtceufix (remove when gtceu fixes them). Showing only gtceu's fluid (if any) is the user's choice; display only, the brewery still accepts other mods' potions via the tag. GTEmiRecipeMixin: EMI R on a potion found nothing because outputs dropped NBT. PotionFluidHint adds the flashing "Any mod's version works" hint (ported 2026-10-20, same as 1.21). Lookups and filling: PotionEmi makes EMI compare potion fluids by all NBT (GTCEu's comparison only looks at the potion, so plain/splash/lingering of one potion were the same fluid and their recipes mixed, user report 2026-11-08), set in addDeferredRecipes because GTCEu's plugin may register after ours. JEI: PotionFluidSubtypeInterpreterMixin appends the container to GTCEu's subtype (JEI keeps the first interpreter registered).