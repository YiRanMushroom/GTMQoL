+++
title = "Splash and lingering potion fluids: gtmqol:potion_container NBT mark, brewed with vanilla container mixes"
keys = [ "bottleitemfluidhandlermixin", "dragon's breath", "gunpowder", "potionitemfluidhandlermixin" ]
updated = "2026-11-08 19:27:27"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionContainers.java"
snippet = "public final class PotionContainers {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionMixBrewingLogic.java"
snippet = "public enum PotionMixBrewingLogic implements GTRecipeType.ICustomRecipeLogic {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/PotionBrewingContainerMixesAccessor.java"
snippet = "@Accessor(\"CONTAINER_MIXES\")"
+++
Port of 1.21's: the mark is an NBT string next to gtceu's Potion key instead of a data component; unmarked = plain potion. Made in the brewery (user's choice) by PotionMixBrewingLogic, a recipe logic runner added after gtceu's BreweryLogic, so it is asked only when that finds nothing. Marked fluids aren't brewed further (gtceu's brewery and FluidIngredient compare the whole tag). Bottle/PotionItem fluid handler mixins fill into / drain from the marked item (gtceu attaches the potion handler to every PotionItem). Mix and its fields are public through Forge's AT; CONTAINER_MIXES via a static accessor.