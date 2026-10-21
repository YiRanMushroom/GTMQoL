+++
title = "Potion fluid names: potion item keys (gtceu bug) plus potency/duration so normal/long/strong differ"
keys = [ "fluid.gtceu.potion.effect", "raw key" ]
updated = "2026-10-19 20:08:35"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/PotionFluidTypeMixin.java"
snippet = "public class PotionFluidTypeMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/PotionFluidNameMixin.java"
snippet = "public class PotionFluidNameMixin {"
+++
PotionFluidTypeMixin is 1.21 only (gtceufix); PotionFluidNameMixin is on both branches.