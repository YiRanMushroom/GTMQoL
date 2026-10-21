+++
title = "@SaveField only saves traits attached with a name or held in a @SaveField field; plain attachTrait isn't saved"
keys = [ "attachpersistenttrait", "markaschanged", "not saved" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientHatchPartMachine.java"
snippet = "// saved through the field: traits attached without a name aren't"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientHandler.java"
snippet = "// markAsChanged: the block entity is only saved again when marked as changed"
+++
General gtceu v8 rule found with the ghost marks. markAsChanged() only marks the block entity dirty; it doesn't make an unnamed trait persistent. The hatch keeps its handler in a @SaveField field; single blocks use attachPersistentTrait("ghostMarks", ...) (ghost.single-block-marks). CyclicMultiblockMachine's PendingOutputTrait follows the field pattern too.