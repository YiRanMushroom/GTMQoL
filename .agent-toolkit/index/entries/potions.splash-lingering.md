+++
title = "Splash and lingering potion fluids: gtmqol:potion_container component, brewed with vanilla container mixes"
keys = [ "bottleitemfluidhandlermixin", "dragon's breath", "potionitemfluidhandlermixin" ]
updated = "2026-11-08 19:26:09"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionContainers.java"
snippet = "public final class PotionContainers {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionMixBrewingLogic.java"
snippet = "public enum PotionMixBrewingLogic implements GTRecipeType.ICustomRecipeLogic {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/PotionBrewingContainerMixesAccessor.java"
snippet = "@Accessor(\"containerMixes\")"

[[links]]
path = "src/main/resources/META-INF/accesstransformer.cfg"
snippet = "public net.minecraft.world.item.alchemy.PotionBrewing$Mix"
+++
Both branches (1.20.1 marks the fluid with an NBT string instead of the component). Made in the brewery (user's choice; a canner item-to-item version was written and removed) by PotionMixBrewingLogic, a recipe logic runner added after gtceu's BreweryLogic, so it is asked only when that finds nothing. Absent component = plain potion. Marked fluids aren't brewed further (gtceu's brewery compares all components). Bottle/PotionItem fluid handler mixins fill into / drain from the marked item. Brewery outputs keep components via gtceufix.fluid-output-components.