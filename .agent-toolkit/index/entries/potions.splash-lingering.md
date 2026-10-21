+++
title = "Splash and lingering potion fluids: gtmqol:potion_container NBT mark, brewed with vanilla container mixes"
keys = [ "bottleitemfluidhandlermixin", "dragon's breath", "gunpowder", "potionitemfluidhandlermixin" ]
updated = "2026-10-19 20:16:06"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionContainers.java"
snippet = "public final class PotionContainers {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/BreweryLogicMixin.java"
snippet = "private void gtmqol$containerMix(IRecipeCapabilityHolder holder, CallbackInfoReturnable<GTRecipe> cir) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/PotionBrewingContainerMixesAccessor.java"
snippet = "@Accessor(\"CONTAINER_MIXES\")"
+++
Port of 1.21's: the mark is an NBT string next to gtceu's Potion key instead of a data component; unmarked = plain potion. Made in the brewery (user's choice). Marked fluids aren't brewed further (gtceu's brewery and FluidIngredient compare the whole tag). Bottle/PotionItem fluid handler mixins fill into / drain from the marked item (gtceu attaches the potion handler to every PotionItem). Mix and its fields are public through Forge's AT; CONTAINER_MIXES via a static accessor.