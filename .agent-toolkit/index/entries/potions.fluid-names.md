+++
title = "Potion fluid names: append potency and duration so normal/long/strong variants differ"
keys = [ "1.21", "getdescription", "potionfluidtype", "potionutils" ]
updated = "2026-10-19 20:16:06"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/PotionFluidNameMixin.java"
snippet = "public class PotionFluidNameMixin {"
+++
Adds a getDescription(FluidStack) override to gtceu's PotionFluid.PotionFluidType, from the first effect in the stack NBT (PotionUtils.getAllEffects), like the potion item tooltip; instant effects get no duration. Splash/lingering fluids (potions.splash-lingering) use their item's name keys. The 1.21 branch also has a gtceufix PotionFluidTypeMixin (potion item name keys); it is not on this branch.