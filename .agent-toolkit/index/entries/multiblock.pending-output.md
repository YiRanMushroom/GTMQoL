+++
title = "PendingOutputTrait: produced items not yet in the output buses, merged by item+components, retried until out"
keys = [ "multiblock base", "savefield", "stored" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/PendingOutputTrait.java"
snippet = "public class PendingOutputTrait extends MachineTrait {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/PendingOutputTrait.java"
snippet = "public boolean output() {"
+++
Parallel @SaveField lists (count-1 templates + long counts) so enchanted books and damaged rods survive. Inserts into every IO.OUT NotifiableItemStackHandler, so ME output buses work. Breaking the controller loses what is pending.