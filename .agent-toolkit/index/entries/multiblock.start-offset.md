+++
title = "Patterns with a repeatable slice before the controller need an explicit startOffset (GTCEu bug)"
keys = [ "multiblock base", "never forms", "originoffset", "slicerepeatable" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = ".startOffset(OriginOffset.of(LEFT, 5))"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = ".startOffset(OriginOffset.of(BACK, 6).move(DOWN, 1).move(LEFT, 3))"
+++
Without startOffset GTCEu guesses the start from the controller's slice index, counting a repeatable slice once, so it checks the wrong blocks and the structure never forms (SAF fixed 2026-10-19; fishing pond). startOffset is the vector from the controller to the first char of the first slice. Any new pattern with sliceRepeatable before the controller's slice needs one.