+++
title = "Pattern crafter: electric crafting table recipes from a self-made crafting pattern, AE2-style encoding, no AE2 needed"
kind = "design"
keys = [ "crafter", "crafting pattern", "fluid substitution", "icustomrecipelogic", "not consumed", "pattern encoding" ]
updated = "2026-11-06 05:45:42"
checked = "2026-11-06"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/ICrafterPattern.java"
snippet = "public interface ICrafterPattern {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/CraftingPattern.java"
snippet = "public @Nullable Plan plan(Level level) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/PatternCrafterLogic.java"
snippet = "enum PatternCrafterLogic implements GTRecipeType.ICustomRecipeLogic {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/CraftingPatternBehavior.java"
snippet = "final class CraftingPatternBehavior implements IItemUIHolder, IAddInformation {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/PatternCrafterEmi.java"
snippet = "public final class PatternCrafterEmi implements EmiPlugin"
+++
Blank and encoded items, encoded in their own window (crafting, stonecutting, smithing); ICrafterPattern is what the logic needs, AE2 patterns adapt to it. Crafting slot use comes from recipe remainders: itself back = not consumed, empty bucket back with fluids on = fluid, else consumed; exact at any durability, substitutes = the recipe's ingredient, special recipes exact. Stonecutting and smithing are exact. The logic picks the runnable pattern (searchRecipe tests only its first return).