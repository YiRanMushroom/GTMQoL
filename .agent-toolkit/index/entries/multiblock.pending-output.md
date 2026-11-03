+++
title = "PendingOutputTrait: produced items not yet in the output buses, merged by item+NBT, retried until out"
keys = [ "multiblock base", "savefield", "stored" ]
updated = "2026-11-03 03:35:02"
checked = "2026-11-03"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/PendingOutputTrait.java"
snippet = "public class PendingOutputTrait extends MachineTrait {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/PendingOutputTrait.java"
snippet = "public boolean output() {"
+++
Parallel @SaveField lists (count-1 templates + long counts) so enchanted books and damaged rods survive; 1.20.1 merges with isSameItemSameTags. Inserts into every IO.OUT NotifiableItemStackHandler, so ME output buses work; the whole rest per slot (not stack-size pieces: thousands of unstackables lagged ME buses). Breaking the controller loses what is pending.