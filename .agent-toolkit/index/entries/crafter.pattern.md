+++
title = "Pattern crafter: electric crafting table recipes from a self-made crafting pattern, AE2-style encoding, no AE2 needed"
kind = "design"
keys = [ "crafter", "crafting pattern", "fluid substitution", "icustomrecipelogic", "kubejs recipe id", "not consumed", "pattern encoding", "pitfall", "smithing substitutes" ]
updated = "2026-11-09 06:44:46"
checked = "2026-11-09"

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
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/PatternWear.java"
snippet = "public final class PatternWear extends MachineTrait"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/CraftingPatternBehavior.java"
snippet = "final class CraftingPatternBehavior implements IItemUIHolder, IAddInformation {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/PatternCrafterEmi.java"
snippet = "public final class PatternCrafterEmi implements EmiPlugin"
+++
Blank and encoded items, encoded in their own window; AE2 patterns adapt to ICrafterPattern. One craft is simulated at encoding and kept on the item (slot uses, outputs); plan() checks it against the recipe, so a changed recipe stops the pattern. Same item back with other data = CHANGED: PatternWear simulates every parallel on the real item at run start, LIMIT cuts the run after OC. Exact (data too) or substitutes (the recipe's ingredient; smithing too since 2026-11-09: template and addition of transform recipes, the base and trims stay exact because their data goes into the result). Pitfall (user report, ATM10): recipes KubeJS adds without an id get <ns>:kjs/<output> numbered _2.. in an unfixed order (parallel streams), so two recipes with one output (ATM10's netherite upgrade template duplication and its diamond-template recipe) swap ids between loads; plan() therefore falls back from byKey(recipeId) to the recipes matching the grid and takes the one that still makes the encoded outputs. Encode option: duplicated inputs kept. Holding Shift shows the product's model like AE2's patterns (CraftingPatternModel wraps the baked item model in ModelEvent.ModifyBakingResult; custom-renderer products such as chests keep the pattern look).