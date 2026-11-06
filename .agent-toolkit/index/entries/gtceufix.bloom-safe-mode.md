+++
title = "BloomSafeModeMixin: one bloom buffer per section rebuild (overlapping rebuilds crashed), and stale block bloom dropped"
keys = [ "bloomrenderer", "bufferbuilder", "crash", "embeddium", "invalidatesectiondata", "not building", "pitfall", "safe_mode", "sodium", "sortquads" ]
updated = "2026-11-06 18:22:22"
checked = "2026-11-06"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/BloomSafeModeMixin.java"
snippet = "private static void gtmqol$bakeOwnBuffer"
+++
1.21 only. The race crashed three times on 2026-11-06 (Not building! twice, IndexOutOfBounds in sortQuads once). 1.20's BloomRenderer shares one builder the same way but has no mixin: never seen crashing there, safe mode is off by default. A cancelled rebuild that ends last can still leave old bloom until the next rebuild.